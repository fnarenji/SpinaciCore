package ensiwow.auth.session

import akka.actor.{FSM, Props}
import ensiwow.auth._
import ensiwow.auth.handlers.{LogonChallenge, LogonProof, ReconnectProof}
import ensiwow.auth.protocol.OpCodes
import ensiwow.auth.protocol.OpCodes.OpCode
import ensiwow.auth.protocol.packets.{ClientChallenge, ClientLogonProof, ClientRealmlistPacket, ClientReconnectProof}
import ensiwow.common.network.{Disconnect, EventPacket, OutgoingPacket, Session}
import ensiwow.utils.{MalformedPacketHeaderException, PacketSerializer}
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
    case Event(e@EventPacket(bits), NoData) =>
      val state = Codec[OpCode].decode(bits) match {
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
    case Event(EventPacket(bits), NoData) =>
      log.debug("Received challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.logonChallengeCodec)
      log.debug(packet.toString)

      logonChallengeHandler ! LogonChallenge(packet)
      stay using NoData
    case Event(EventChallengeSuccess(packet, challengeData), NoData) =>
      log.debug(s"Sending successful challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)

      goto(StateProof) using challengeData
    case Event(EventChallengeFailure(packet), NoData) =>
      log.debug(s"Sending failed challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateProof) {
    case Event(EventPacket(bits), challengeData: ChallengeData) =>
      log.debug("Received proof")
      val packet = PacketSerializer.deserialize[ClientLogonProof](bits)
      log.debug(packet.toString)

      logonProofHandler ! LogonProof(packet, challengeData)
      stay using challengeData
    case Event(EventProofSuccess(packet, proofData), _: ChallengeData) =>
      log.debug(s"Sending successful proof $packet")
      val bits = PacketSerializer.serialize(packet)

      // TODO: shared key should be saved to database
      context.parent ! OutgoingPacket(bits)
      goto(StateRealmlist) using NoData
    case Event(EventProofFailure(packet), _: ChallengeData) =>
      log.debug(s"Sending failed proof $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateReconnectChallenge) {
    case Event(EventPacket(bits), NoData) =>
      log.debug("Received reconnect challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.reconnectChallengeCodec)
      log.debug(packet.toString)

      reconnectChallengeHandler ! LogonChallenge(packet)
      stay using NoData
    case Event(EventChallengeSuccess(packet, challengeData), NoData) =>
      log.debug(s"Sending successful reconnect challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)

      goto(StateReconnectProof) using challengeData
    case Event(EventChallengeFailure(packet), NoData) =>
      log.debug(s"Sending failed reconnect challenge $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateReconnectProof) {
    case Event(EventPacket(bits), challengeData: ReconnectChallengeData) =>
      log.debug("Received reconnect proof")
      val packet = PacketSerializer.deserialize[ClientReconnectProof](bits)
      log.debug(packet.toString)

      reconnectProofHandler ! ReconnectProof(packet, challengeData)
      stay using challengeData
    case Event(EventReconnectProofSuccess(packet), _: ReconnectChallengeData) =>
      log.debug(s"Sending successful reconnect proof $packet")
      val bits = PacketSerializer.serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateRealmlist) using NoData
    case Event(EventReconnectProofFailure, _: ReconnectChallengeData) =>
      log.debug(s"Failed reconnect proof, disconnecting")
      goto(StateFailed)
  }

  when(StateRealmlist) {
    case Event(EventPacket(bits), NoData) =>
      val packet = PacketSerializer.deserialize[ClientRealmlistPacket](bits)
      log.debug(s"Received realm list request: $packet")

      authServer ! GetRealmlist
      stay using NoData
    case Event(EventRealmlist(bits), NoData) =>
      context.parent ! OutgoingPacket(bits)
      stay using NoData
  }

  when(StateFailed, stateTimeout = 5 second) {
    case Event(StateTimeout, _) =>
      log.debug("Failed state expired, disconnecting")
      context.parent ! Disconnect
      stop
  }
}

object AuthSession extends Session {
  override def props: Props = Props(classOf[AuthSession])

  override def PreferredName = "AuthSession"
}
