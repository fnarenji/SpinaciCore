package wow.auth.session

import akka.actor.{FSM, Props}
import wow.auth._
import wow.auth.handlers._
import wow.auth.protocol.{OpCodes, ServerPacket}
import wow.auth.protocol.packets.{ClientChallenge, ClientLogonProof, ClientRealmlist, ClientReconnectProof}
import wow.auth.utils.{MalformedPacketHeaderException, PacketSerializer}
import wow.common.network.{EventIncoming, SessionActorCompanion, TCPHandler}
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult, Err}
import wow.auth.crypto.Srp6Protocol

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Handles an auth session
  */
class AuthSession extends FSM[AuthSessionState, AuthSessionData]
                          with LogonChallengeHandler
                          with LogonProofHandler
                          with ReconnectChallengeHandler
                          with ReconnectProofHandler {
  val srp6: Srp6Protocol = new Srp6Protocol()

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

  when(StateChallenge)(handleChallenge)

  when(StateProof)(handleProof)

  when(StateReconnectChallenge)(handleReconnectChallenge)

  when(StateReconnectProof)(handleReconnectProof)

  when(StateRealmlist) {
    case Event(EventIncoming(bits), NoData) =>
      val packet = PacketSerializer.deserialize[ClientRealmlist](bits)
      log.debug(s"Received realm list request: $packet")

      context.parent ! TCPHandler.OutgoingPacket(AuthServer.realmlistPacketBits)
      stay using NoData
  }

  when(StateFailed, stateTimeout = 5 second) {
    case Event(StateTimeout, _) =>
      log.debug("Failed state expired, disconnecting")
      context.parent ! TCPHandler.Disconnect
      stop
  }

  def sendPacket[A <: ServerPacket](packet: A)(implicit codec: Codec[A]): Unit = {
    log.debug(s"Sending $packet")
    val bits = PacketSerializer.serialize(packet)(codec)

    context.parent ! TCPHandler.OutgoingPacket(bits)
  }
}

object AuthSession extends SessionActorCompanion {
  override def props: Props = Props(classOf[AuthSession])

  override def PreferredName = "AuthSession"
}

