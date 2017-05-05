package ensiwow.auth.session

import akka.actor.{FSM, Props}
import ensiwow.auth._
import ensiwow.auth.handlers.{LogonChallenge, LogonProof, ReconnectProof}
import ensiwow.auth.protocol.OpCodes
import ensiwow.auth.protocol.packets.{ClientChallenge, ClientLogonProof, ClientRealmlist, ClientReconnectProof}
import ensiwow.auth.utils.{MalformedPacketHeaderException, PacketSerializer}
import ensiwow.common.network.{EventIncoming, SessionActorCompanion, TCPHandler}
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult, Err}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Handles an auth session
  */
class AuthSession extends FSM[AuthSessionState, AuthSessionData] {
  private val logonChallengeHandler = context.actorSelection(AuthServer.LogonChallengeHandlerPath)
  private val logonProofHandler = context.actorSelection(AuthServer.LogonProofHandlerPath)
  private val reconnectChallengeHandler = context.actorSelection(AuthServer.ReconnectChallengeHandlerPath)
  private val reconnectProofHandler = context.actorSelection(AuthServer.ReconnectProofHandlerPath)
  private val authServer = context.actorSelection(AuthServer.ActorPath)

  // First packet that we expect from client is logon challenge
  startWith(StateNoData, NoData)

  when(StateNoData) {
    case Event(e@EventIncoming(bits), NoData) =>
      val state = Codec[OpCodes.Value].decode(bits) match {
        case Successful(DecodeResult(OpCodes.LogonChallenge, _)) => StateChallenge
        case Successful(DecodeResult(OpCodes.ReconnectChallenge, _)) => StateReconnectChallenge
        case Failure(err) => throw MalformedPacketHeaderException(err)
        case _ => throw MalformedPacketHeaderException(Err("Expected either logon or reconnect challenge"))
      }
      log.debug(s"Got first packet, going to $state")

      self ! e
      goto(state)
  }

  when(StateChallenge) {
    case Event(EventIncoming(bits), NoData) =>
      log.debug("Received challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.logonChallengeCodec)
      log.debug(packet.toString)

      logonChallengeHandler ! LogonChallenge(packet)
      stay using NoData
    case Event(EventChallengeSuccess(packet, challengeData), NoData) =>
      log.debug(s"Sending successful challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)

      goto(StateProof) using challengeData
    case Event(EventChallengeFailure(packet), NoData) =>
      log.debug(s"Sending failed challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateProof) {
    case Event(EventIncoming(bits), challengeData: ChallengeData) =>
      log.debug("Received proof")
      val packet = PacketSerializer.deserialize[ClientLogonProof](bits)
      log.debug(packet.toString)

      logonProofHandler ! LogonProof(packet, challengeData)
      stay using challengeData
    case Event(EventProofSuccess(packet), _: ChallengeData) =>
      log.debug(s"Sending successful proof $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)
      goto(StateRealmlist) using NoData
    case Event(EventProofFailure(packet), _: ChallengeData) =>
      log.debug(s"Sending failed proof $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateReconnectChallenge) {
    case Event(EventIncoming(bits), NoData) =>
      log.debug("Received reconnect challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.reconnectChallengeCodec)
      log.debug(packet.toString)

      reconnectChallengeHandler ! LogonChallenge(packet)
      stay using NoData
    case Event(EventChallengeSuccess(packet, challengeData), NoData) =>
      log.debug(s"Sending successful reconnect challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)

      goto(StateReconnectProof) using challengeData
    case Event(EventChallengeFailure(packet), NoData) =>
      log.debug(s"Sending failed reconnect challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateReconnectProof) {
    case Event(EventIncoming(bits), challengeData: ReconnectChallengeData) =>
      log.debug("Received reconnect proof")
      val packet = PacketSerializer.deserialize[ClientReconnectProof](bits)
      log.debug(packet.toString)

      reconnectProofHandler ! ReconnectProof(packet, challengeData)
      stay using challengeData
    case Event(EventReconnectProofSuccess(packet), _: ReconnectChallengeData) =>
      log.debug(s"Sending successful reconnect proof $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! TCPHandler.OutgoingPacket(bits)
      goto(StateRealmlist) using NoData
    case Event(EventReconnectProofFailure, _: ReconnectChallengeData) =>
      log.debug(s"Failed reconnect proof, disconnecting")
      goto(StateFailed)
  }

  when(StateRealmlist) {
    case Event(EventIncoming(bits), NoData) =>
      val packet = PacketSerializer.deserialize[ClientRealmlist](bits)
      log.debug(s"Received realm list request: $packet")

      authServer ! GetRealmlist
      stay using NoData
    case Event(EventRealmlist(bits), NoData) =>
      context.parent ! TCPHandler.OutgoingPacket(bits)
      stay using NoData
  }

  when(StateFailed, stateTimeout = 5 second) {
    case Event(StateTimeout, _) =>
      log.debug("Failed state expired, disconnecting")
      context.parent ! TCPHandler.Disconnect
      stop
  }
}


object AuthSession extends SessionActorCompanion {
  override def props: Props = Props(classOf[AuthSession])

  override def PreferredName = "AuthSession"
}
