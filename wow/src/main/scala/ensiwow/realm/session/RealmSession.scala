package ensiwow.realm.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.common.network.{Disconnect, EventIncoming, OutgoingPacket, SessionActorCompanion}
import ensiwow.realm.RealmServer
import ensiwow.realm.crypto.SessionCipher
import ensiwow.realm.protocol._
import ensiwow.realm.protocol.payloads.ServerAuthChallenge
import scodec.bits.BitVector
import scodec.{Codec, Err}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

sealed trait RealmSessionEvent

sealed class PayloadBearingEvent[T <: Payload[ServerHeader]](payload: T)
  (implicit codec: Codec[T], opCodeProvider: OpCodeProvider[T])
  extends RealmSessionEvent {
  def serialize(sessionCipher: Option[SessionCipher]): BitVector =
    PacketSerialization.outgoing(payload)(sessionCipher)(implicitly, implicitly)
}

case class EventOutgoing[T <: Payload[ServerHeader]](payload: T)
  (implicit codec: Codec[T], opCodeProvider: OpCodeProvider[T]) extends PayloadBearingEvent[T](payload)

case class EventHandlerFailure(err: Err) extends RealmSessionEvent
case object EventEmptyHandlerFailure extends RealmSessionEvent

case class EventTerminate(delayed: Boolean) extends RealmSessionEvent

case class EventTerminateWithPayload[T <: Payload[ServerHeader]](payload: T)
  (implicit codec: Codec[T], opCodeProvider: OpCodeProvider[T]) extends PayloadBearingEvent[T](payload)

case class EventAuthenticated(sessionKey: BigInt) extends RealmSessionEvent

/**
  * Handles a realm session
  */
class RealmSession extends Actor with ActorLogging {
  private val terminationDelay = 5 second
  private var sessionCipher: Option[SessionCipher] = None

  // Send initial challenge packet
  sendAuthChallenge

  override def receive: Receive = {
    case EventIncoming(bits) =>
      val (header, payloadBits) = PacketSerialization.incomingHeader(bits)(sessionCipher)

      if (PayloadHandlerHelper.isHandled(header.opCode)) {
        log.debug(s"Got packet $header/${payloadBits.bytes.length}")

        val handlerPath = RealmServer.handlerPath(header.opCode)
        val handler = context.actorSelection(handlerPath)

        handler ! EventPacket(payloadBits)
      } else {
        log.info(s"Got unhandled packet $header/${payloadBits.bytes.length}")
      }

    case ev@EventOutgoing(payload) =>
      log.debug(s"Sending $payload")
      val bits = ev.serialize(sessionCipher)

      context.parent ! OutgoingPacket(bits)

    case EventTerminate(delayed) =>
      if (delayed) {
        context.system.scheduler.scheduleOnce(terminationDelay)(terminate)(context.dispatcher)
      } else {
        terminate
      }

    case ev@EventTerminateWithPayload(payload) =>
      log.debug(s"Sending $payload")
      val bits = ev.serialize(sessionCipher)

      context.parent ! OutgoingPacket(bits)

      context.system.scheduler.scheduleOnce(terminationDelay)(terminate)(context.dispatcher)

    case EventHandlerFailure(err: Err) =>
      log.debug(s"Failure to handle packet ($err), disconnect client")
      terminate

    case EventAuthenticated(sessionKey) =>
      log.debug(s"Session key set up: $sessionKey")
      sessionCipher = Some(new SessionCipher(sessionKey))
  }

  private def terminate = {
    context.parent ! Disconnect
    context.stop(self)
  }

  private def sendAuthChallenge = {
    val SeedSizeBits = ServerAuthChallenge.SeedSize * 8

    val UInt32MaxValue = 0x7FFFFFFFL

    val authChallenge = PacketSerialization.outgoing(
      ServerAuthChallenge(
        ThreadLocalRandom.current().nextLong(UInt32MaxValue),
        BigInt(SeedSizeBits, Random),
        BigInt(SeedSizeBits, Random))
    )(sessionCipher)

    context.parent ! OutgoingPacket(authChallenge)
  }
}

object RealmSession extends SessionActorCompanion {
  override def props: Props = Props(classOf[RealmSession])

  override def PreferredName = "RealmSession"
}
