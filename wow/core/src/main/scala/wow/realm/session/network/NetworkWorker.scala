package wow.realm.session.network

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import scodec.bits.BitVector
import wow.common.network.{TCPSession, TCPSessionFactory}
import wow.realm.crypto.SessionCipher
import wow.realm.protocol.payloads.ServerAuthChallenge
import wow.realm.protocol.{OpCodes, _}
import wow.realm.{RealmContext, RealmContextData}

import scala.util.Random

/**
  * Handles a realm session's networking
  * Most of the features of this actor are actual provided by composing in traits
  */
class NetworkWorker(override val connection: ActorRef)(override implicit val realm: RealmContextData)
  extends TCPSession
          with HandleIncomingPackets
          with SendForwardedPackets
          with SendPacketsToClient
          with Actor
          with ActorLogging
          with RealmContext {
  private[session] var sessionCipher: Option[SessionCipher] = None

  override def receive: Receive = Seq[Receive](
    tcpSessionReceiver,
    sendForwardedPacketsReceiver,
    {
      case Terminated(subject) if subject == session =>
        log.debug(s"$subject died, stopping self ($self)")
        context.stop(self)
      case Terminated(subject) if subject == player =>
        player = None
    }
  ).reduceLeft(_.orElse(_))

  override def preStart(): Unit = {
    val SeedSizeBits = ServerAuthChallenge.SeedSize * 8

    val authChallenge = ServerAuthChallenge(
      authSeed,
      BigInt(SeedSizeBits, Random),
      BigInt(SeedSizeBits, Random)
    )

    sendPayload(authChallenge)

    super.preStart()
  }

  override def postStop(): Unit = {
    _session.foreach(context.stop)

    super.postStop()
  }

  /**
    * This must remain lazy or be moved before the call to sendAuthChallenge().
    * Otherwise, initialization order will make challenge digest validation fail.
    */
  lazy val authSeed: Long = {
    val UInt32MaxValue = 0x7FFFFFFFL
    ThreadLocalRandom.current().nextLong(UInt32MaxValue)
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
  def player: Option[ActorRef] = {
    _player
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
}

object NetworkWorker {

  case class SendRawPayload(payloadBits: BitVector, opCode: OpCodes.Value)

  case class SendRaw(bits: BitVector)

  case class SendRawSplit(headerBits: BitVector, payloadBits: BitVector)

  case class Terminate(delayed: Boolean)

  case class HandlePacket(header: ClientHeader, payloadBits: BitVector)

}

class NetworkWorkerFactory(implicit realm: RealmContextData) extends TCPSessionFactory {
  override def props(connection: ActorRef): Props = Props(new NetworkWorker(connection))

  override val PreferredName = "networkworker"
}
