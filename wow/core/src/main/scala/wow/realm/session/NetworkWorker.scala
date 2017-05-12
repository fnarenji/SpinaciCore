package wow.realm.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scodec.Codec
import scodec.bits.BitVector
import wow.common.network.{EventIncoming, TCPSession, TCPSessionFactory}
import wow.realm.crypto.SessionCipher
import wow.realm.handlers.HandledBy
import wow.realm.protocol.payloads.ServerAuthChallenge
import wow.realm.protocol.{OpCodes, _}
import wow.realm.session.NetworkWorker.HandlePacket
import wow.realm.{RealmContext, RealmContextData}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * Handles a realm session's networking
  */
class NetworkWorker(connection: ActorRef)(override implicit val realm: RealmContextData)
  extends TCPSession(connection)
          with Actor
          with ActorLogging
          with PacketHandlerTag
          with RealmContext
          with CanSendPackets {
  private var sessionCipher: Option[SessionCipher] = None

  // TODO: This should to be extracted to a trait and use a FSM
  private var unprocessedBits = BitVector.empty
  private var currHeader: Option[ClientHeader] = None

  // Send initial challenge packet
  sendAuthChallenge()

  override def receive: Receive = super[TCPSession].receive orElse {
    case EventIncoming(bits) =>
      unprocessedBits = unprocessedBits ++ bits
      processBufferedBits()

    case ev: NetworkWorker.SendPayload[_] =>
      sendRaw(ev.serialize(sessionCipher))

    case NetworkWorker.SendRawPayload(payloadBits, opCode) =>
      sendRaw(payloadBits, opCode)

    case NetworkWorker.SendRaw(bits) =>
      sendRaw(bits)

    case NetworkWorker.SendSplit(headerBits, payloadBits) =>
      val bits = PacketSerialization.outgoing(headerBits, payloadBits)(sessionCipher)

      sendRaw(bits)

    case NetworkWorker.Terminate(delayed) =>
      if (delayed) {
        terminateDelayed()
      } else {
        terminateNow()
      }

    case ev: NetworkWorker.TerminateWithPayload[_] =>
      sendRaw(ev.serialize(sessionCipher))
      terminateDelayed()
  }

  private def processBufferedBits(): Unit = {
    //    log.debug(s"Have ${unprocessedBits.bytes.size} bytes waiting to be processed")
    if (currHeader.isEmpty && unprocessedBits.sizeGreaterThanOrEqual(Codec[ClientHeader].sizeBound.exact.get)) {
      //      log.debug("No header, parsing next one")
      val (header, remaining) = PacketSerialization.incomingHeader(unprocessedBits)(sessionCipher)
      currHeader = Some(header)
      unprocessedBits = remaining
    }

    currHeader match {
      case Some(header) =>
        if (unprocessedBits.bytes.size >= header.payloadSize) {
          // log.debug("Has header and payload, parsing payload")

          val payloadBits = unprocessedBits.take(header.payloadSize * 8L)
          unprocessedBits = unprocessedBits.drop(header.payloadSize * 8L)
          currHeader = None

          PacketHandler(header) match {
            case HandledBy.NetworkWorker =>
              PacketHandler(header, payloadBits)(this)
            case HandledBy.Session =>
              session ! HandlePacket(header, payloadBits)
            case HandledBy.Player =>
              player ! HandlePacket(header, payloadBits)
            case HandledBy.Unhandled =>
              log.info(s"Unhandled packet ${header.opCode}")
          }

          processBufferedBits()
        } else {
          //          log.debug("Has header, no payload, not enough data")
        }
      case None =>
      //        log.debug("No header, not enough data for next one")
    }
  }

  /**
    * This must remain lazy or be moved before the call to sendAuthChallenge().
    * Otherwise, initialization order will make challenge digest validation fail.
    */
  lazy val authSeed: Long = {
    val UInt32MaxValue = 0x7FFFFFFFL
    ThreadLocalRandom.current().nextLong(UInt32MaxValue)
  }

  private def sendAuthChallenge() = {
    val SeedSizeBits = ServerAuthChallenge.SeedSize * 8

    val authChallenge = ServerAuthChallenge(
      authSeed,
      BigInt(SeedSizeBits, Random),
      BigInt(SeedSizeBits, Random)
    )

    sendPayload(authChallenge)
  }

  private val terminationDelay = 1 second
  override def terminateDelayed(): Unit = {
    context.system.scheduler.scheduleOnce(terminationDelay)(terminateNow())(context.dispatcher)
  }

  override def terminateNow(): Unit = {
    disconnect()
    context.stop(self)
  }

  override def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit = {
    log.debug("Sending " + payload)
    val bits = PacketSerialization.outgoing(payload)(sessionCipher)

    sendRaw(bits)
  }

  override def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit = {
    val bits = PacketSerialization.outgoing(payloadBits, opCode)(sessionCipher)

    sendRaw(bits)
  }

  override def sendRaw(bits: BitVector): Unit = outgoing(bits)

  /**
    * Enables the cipher for all packets onwards
    *
    * @param sessionKey session key to use
    */
  def enableCipher(sessionKey: BigInt): Unit = {
    require(sessionCipher.isEmpty)
    log.debug(s"Session key set up: $sessionKey")
    sessionCipher = Some(new SessionCipher(sessionKey))
  }

  /**
    * The session associated to this network worker
    */
  private var _session: Option[ActorRef] = None

  /**
    * Getter for session associated with network worker. Will throw if not present.
    *
    * @return session associated with network worker
    */
  def session: ActorRef = {
    if (_session.isEmpty) {
      throw new IllegalStateException("Session actor ref is not set")
    }
    _session.get
  }

  /**
    * Setter for session associated with network worker. Will throw if already present.
    *
    * @param sessionRef session associated with network worker
    */
  def session_=(sessionRef: ActorRef): Unit = _session = {
    if (_session.nonEmpty) {
      throw new IllegalStateException("Session actor ref can only be set once")
    }

    Some(sessionRef)
  }

  /**
    * Current SessionPlayer actor ref
    */
  private var _player: Option[ActorRef] = None

  /**
    * Getter for current session player actor. Will throw if not present.
    *
    * @return current session player actor ref
    */
  def player: ActorRef = {
    if (_player.isEmpty) {
      throw new IllegalStateException("Player actor ref is not set")
    }

    _player.get
  }

  /**
    * Setter for current session player actor. Will throw if already present.
    *
    * @param playerRef current session player actor ref
    */
  def player_=(playerRef: ActorRef): Unit = {
    if (_player.nonEmpty) {
      throw new IllegalStateException("Player actor ref is already set and should not be overwritten")
    }

    _player = Some(playerRef)
  }

  /**
    * Removes current session player actor ref.
    *
    * @param none None
    */
  def player_=(none: Option[Nothing]): Unit = {
    require(none.isEmpty)
    _player = None
  }
}

object NetworkWorker {

  sealed class PayloadBearingMessage[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]) {
    def serialize(sessionCipher: Option[SessionCipher]): BitVector = {
      PacketSerialization.outgoing(payload)(sessionCipher)(implicitly, implicitly)
    }
  }

  case class SendPayload[A <: Payload with ServerSide](payload: A)
    (implicit opCodeProvider: OpCodeProvider[A], codec: Codec[A])
    extends PayloadBearingMessage[A](payload)

  case class SendRawPayload(payloadBits: BitVector, opCode: OpCodes.Value)

  case class SendRaw(bits: BitVector)

  case class SendSplit(headerBits: BitVector, payloadBits: BitVector)

  case class Terminate(delayed: Boolean)

  case class TerminateWithPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]) extends PayloadBearingMessage[A](payload)

  case class HandlePacket(header: ClientHeader, payloadBits: BitVector)

}

class NetworkWorkerFactory(implicit realm: RealmContextData) extends TCPSessionFactory {
  override def props(connection: ActorRef): Props = Props(new NetworkWorker(connection))

  override val PreferredName = "NetworkWorker"
}
