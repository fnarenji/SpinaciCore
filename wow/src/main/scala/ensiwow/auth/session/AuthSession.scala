package ensiwow.auth.session

import akka.actor.{FSM, Props}
import ensiwow.auth._
import ensiwow.auth.handlers.LogonChallenge
import ensiwow.auth.network.{Disconnect, OutgoingPacket}
import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ClientPacket, ServerPacket}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}
import scala.concurrent.duration._

/**
  * Handles an auth session
  */
class AuthSession extends FSM[AuthSessionState, AuthSessionData] {
  private val logonChallengeHandler = context.actorSelection(AuthServer.LogonChallengeHandlerPath)

  // First packet that we expect from client is logon challenge
  startWith(StateChallenge, InitData)

  when(StateChallenge) {
    case Event(EventPacket(bits), InitData) =>
      log.debug("Received challenge")
      val packet = deserialize[ClientLogonChallenge](bits)
      log.debug(packet.toString)

      logonChallengeHandler ! LogonChallenge(packet, InitData.g, InitData.N)
      stay using InitData
    case Event(EventChallengeSuccess(packet, challengeData), InitData) =>
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)

      goto(StateProof) using challengeData
    case Event(EventChallengeFailure(packet), InitData) =>
      log.debug(s"Sending failed challenge $packet")
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateProof) {
    case Event(EventPacket(bits), challengeData) =>
      log.debug("Received proof")
      stay using challengeData
  }

  when(StateFailed, stateTimeout = 1 second) {
    case Event(StateTimeout, _) =>
      log.debug("Failed state expired, disconnecting")
      context.parent ! Disconnect
      stop
  }

  /**
    * Deserializes a packet of type T from the bitvector
    *
    * @param bits  bitvector from which to read
    * @param codec codec used for deserialization
    * @tparam T type of packet
    * @return deserialized packets
    */
  private def deserialize[T <: ClientPacket](bits: BitVector)(implicit codec: Codec[T]): T = {
    codec.decode(bits) match {
      case Successful(DecodeResult(value, BitVector.empty)) => value
      case Successful(DecodeResult(_, remainder)) => throw PacketPartialReadException(remainder)
      case Failure(err) => throw MalformedPacketException(err)
    }
  }

  /**
    * Serializes a packet of type T to a bitvector
    *
    * @param value packet to be serialized
    * @param codec codec
    * @tparam T packet type
    * @return bit vector containing serialized object
    */
  private def serialize[T <: ServerPacket](value: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(value) match {
      case Successful(bits) => bits
      case Failure(err) => throw PacketSerializationException(err)
    }
  }
}

object AuthSession {
  def props: Props = Props(classOf[AuthSession])

  val PreferredName = "AuthSession"
}
