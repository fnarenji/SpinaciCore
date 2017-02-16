package ensiwow.auth.session

import akka.actor.{FSM, Props}
import ensiwow.auth._
import ensiwow.auth.handlers.{LogonChallenge, LogonProof}
import ensiwow.auth.network.{Disconnect, OutgoingPacket}
import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ClientLogonProof}
import ensiwow.auth.protocol.{ClientPacket, ServerPacket}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Handles an auth session
  */
class AuthSession extends FSM[AuthSessionState, AuthSessionData] {
  private val logonChallengeHandler = context.actorSelection(AuthServer.LogonChallengeHandlerPath)
  private val logonProofHandler = context.actorSelection(AuthServer.LogonProofHandlerPath)

  // First packet that we expect from client is logon challenge
  startWith(StateChallenge, NoData)

  when(StateChallenge) {
    case Event(EventPacket(bits), NoData) =>
      // TODO: here we should distinguish between LogonChallenge and ReconnectChallenge by using the opcode
      log.debug("Received challenge")
      val packet = deserialize[ClientLogonChallenge](bits)
      log.debug(packet.toString)

      logonChallengeHandler ! LogonChallenge(packet)
      stay using NoData
    case Event(EventChallengeSuccess(packet, challengeData: ChallengeData), NoData) =>
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)

      goto(StateProof) using challengeData
    case Event(EventChallengeFailure(packet), NoData) =>
      log.debug(s"Sending failed challenge $packet")
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateProof) {
    case Event(EventPacket(bits), challengeData: ChallengeData) =>
      log.debug("Received proof")
      val packet = deserialize[ClientLogonProof](bits)
      log.debug(packet.toString)

      logonProofHandler ! LogonProof(packet, challengeData)
      stay using challengeData
    case Event(EventLogonSuccess(packet), proofData: ProofData) =>
      log.debug(s"Sending successful logon $packet")
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateRealmlist) using proofData
    case Event(EventLogonFailure(packet), _) =>
      log.debug(s"Sending failed logon $packet")
      val bits = serialize(packet)

      context.parent ! OutgoingPacket(bits)
      goto(StateFailed)
  }

  when(StateRealmlist) {
    case Event(EventPacket(bits), _: ProofData) =>
      import scodec.bits._
      assert(bits == hex"1000000000".bits)
      log.debug("Realmlist R&R.")
      context.parent !
        OutgoingPacket(hex"1029000000000001000100025472696E697479003132372E302E302E313A3830383500000000000101011000"
          .bits)
      stay
  }

  when(StateFailed, stateTimeout = 5 second) {
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
