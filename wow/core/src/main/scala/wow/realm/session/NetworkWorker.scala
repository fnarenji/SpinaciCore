package wow.realm.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, Props}
import wow.common.network.{EventIncoming, SessionActorCompanion, TCPHandler}
import wow.realm.RealmServer
import wow.realm.crypto.SessionCipher
import wow.realm.protocol._
import wow.realm.protocol.payloads.ServerAuthChallenge
import scodec.bits.BitVector
import scodec.{Codec, Err}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random


/**
  * Handles a realm session's networking
  */
class NetworkWorker extends Actor with ActorLogging {
  context.actorOf(Session.props, Session.PreferredName)

  private val terminationDelay = 5 second
  private var sessionCipher: Option[SessionCipher] = None

  private var unprocessedBits = BitVector.empty
  private var currHeader: Option[ClientHeader] = None

  // Send initial challenge packet
  sendAuthChallenge()

  override def receive: Receive = {
    case EventIncoming(bits) =>
      unprocessedBits = unprocessedBits ++ bits

      processBufferedBits()

    case ev@NetworkWorker.EventOutgoing(payload) =>
      log.debug(s"Sending $payload")
      val bits = ev.serialize(sessionCipher)

      context.parent ! TCPHandler.OutgoingPacket(bits)

    case NetworkWorker.EventOutgoingRaw(payloadBits, opCode) =>
      val bits = PacketSerialization.outgoing(payloadBits, opCode)(sessionCipher)

      context.parent ! TCPHandler.OutgoingPacket(bits)

    case NetworkWorker.EventOutgoingSplit(headerBits, payloadBits) =>
      val bits = PacketSerialization.outgoing(headerBits, payloadBits)(sessionCipher)

      context.parent ! TCPHandler.OutgoingPacket(bits)

    case NetworkWorker.EventTerminate(delayed) =>
      if (delayed) {
        context.system.scheduler.scheduleOnce(terminationDelay)(terminate())(context.dispatcher)
      } else {
        terminate()
      }

    case ev@NetworkWorker.EventTerminateWithPayload(payload) =>
      log.debug(s"Sending $payload")
      val bits = ev.serialize(sessionCipher)

      context.parent ! TCPHandler.OutgoingPacket(bits)

      context.system.scheduler.scheduleOnce(terminationDelay)(terminate())(context.dispatcher)

    case NetworkWorker.EventHandlerFailure(err: Err) =>
      log.debug(s"Failure to handle packet ($err), disconnect client")
      terminate()

    case NetworkWorker.EventAuthenticated(sessionKey) =>
      log.debug(s"Session key set up: $sessionKey")
      sessionCipher = Some(new SessionCipher(sessionKey))
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

          if (PacketHandlerHelper.isHandled(header.opCode)) {
            //            log.debug(s"Got packet $header/${payloadBits.bytes.length}")

            val handlerPath = RealmServer.handlerPath(header.opCode)
            val handler = context.actorSelection(handlerPath)

            handler ! EventPacket(header, payloadBits)
          } else {
            log.info(s"Got unhandled packet $header/${payloadBits.bytes.length}")
          }

          processBufferedBits()
        } else {
          //          log.debug("Has header, no payload, not enough data")
        }
      case None =>
      //        log.debug("No header, not enough data for next one")
    }
  }

  private def terminate() = {
    context.parent ! TCPHandler.Disconnect
    context.stop(self)
  }

  private def sendAuthChallenge() = {
    val SeedSizeBits = ServerAuthChallenge.SeedSize * 8

    val UInt32MaxValue = 0x7FFFFFFFL

    val authChallenge = ServerAuthChallenge(
      ThreadLocalRandom.current().nextLong(UInt32MaxValue),
      BigInt(SeedSizeBits, Random),
      BigInt(SeedSizeBits, Random))

    val bits = PacketSerialization.outgoing(authChallenge)(sessionCipher)

    context.parent ! TCPHandler.OutgoingPacket(bits)
  }
}

object NetworkWorker extends SessionActorCompanion {
  override def props: Props = Props(classOf[NetworkWorker])

  override def PreferredName = "SessionNetworkWorker"

  sealed trait RealmSessionEvent

  sealed class PayloadBearingEvent[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A])
    extends RealmSessionEvent {
    def serialize(sessionCipher: Option[SessionCipher]): BitVector = {
      PacketSerialization.outgoing(payload)(sessionCipher)(implicitly, implicitly)
    }
  }

  case class EventOutgoing[A <: Payload with ServerSide](payload: A)
    (implicit opCodeProvider: OpCodeProvider[A], codec: Codec[A])
    extends PayloadBearingEvent[A](payload)

  case class EventOutgoingRaw(bits: BitVector, opCode: OpCodes.Value) extends RealmSessionEvent

  case class EventOutgoingSplit(headerBits: BitVector, payloadBits: BitVector) extends RealmSessionEvent

  case class EventHandlerFailure(err: Err) extends RealmSessionEvent

  case object EventEmptyHandlerFailure extends RealmSessionEvent

  case class EventTerminate(delayed: Boolean) extends RealmSessionEvent

  case class EventTerminateWithPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]) extends PayloadBearingEvent[A](payload)

  case class EventAuthenticated(sessionKey: BigInt) extends RealmSessionEvent

}
