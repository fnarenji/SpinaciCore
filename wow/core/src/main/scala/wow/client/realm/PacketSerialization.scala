package wow.client.realm

import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}
import wow.auth.protocol.OpCodes
import wow.auth.utils.{MalformedPacketHeaderException, PacketPartialReadException, PacketSerializationException}
import wow.realm.crypto.SessionCipher
import wow.realm.protocol.{ClientSide, Payload}

object PacketSerialization {

  /**
    * Constructs outgoing packet
    *
    * @param headerBits  header bits
    * @param payloadBits payload bits
    * @param cipher      optional cipher to apply
    * @return packet bits
    */
  def outgoing(headerBits: BitVector, payloadBits: BitVector)(cipher: Option[SessionCipher]): BitVector = {
    assert(headerBits.size == ClientHeader.OpCodeSize)

    val encryptedHeader = headerBits.toByteArray

    cipher foreach { cipher => cipher.encrypt(encryptedHeader) }

    BitVector(encryptedHeader) ++ payloadBits
  }

  /**
    * Serializes an outgoing packet
    *
    * @param payload        payload to serialize
    * @param cipher         optional encryption cipher to be used
    * @param payloadCodec   codec used to serialize payload
    * @param opCodeProvider opcode provider for payload type
    * @tparam A payload type
    * @return bits of packet containing (encrypted) header and serialized payload
    */
  def outgoing[A <: Payload with ClientSide](payload: A)(cipher: Option[SessionCipher])(
    implicit payloadCodec: Codec[A],
    opCodeProvider: OpCodeProvider[A]): BitVector = {
    payloadCodec.encode(payload) match {
      case Successful(payloadBits) =>
        outgoing(payloadBits, opCodeProvider.opCode)(cipher)
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  /**
    * Serializes an outgoing packet
    *
    * @param payloadBits payload bits
    * @param opCode      opcode of payload
    * @param cipher      optional encryption cipher to be used
    * @return bits of packet containing (encrypted) header and serialized payload
    */
  def outgoing(payloadBits: BitVector, opCode: OpCodes.Value)(cipher: Option[SessionCipher]): BitVector = {
    val header = ClientHeader(payloadBits.bytes.intSize.get, opCode)

    Codec[ClientHeader].encode(header) match {
      case Successful(headerBits) =>
        outgoing(headerBits, payloadBits)(cipher)
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  /**
    * Deserializes an incoming packet header
    *
    * @param bits   bits of incoming packet
    * @param cipher optional encryption cipher to be used
    * @return tuple containing deserialized header and unprocessed bits
    */
  def incomingHeader(bits: BitVector)(cipher: Option[SessionCipher]): (ServerHeader, BitVector) = {
    val headerLength = Codec[ServerHeader].sizeBound.exact.get

    val headerBits = bits.take(headerLength)
    val headerBytesArray = headerBits.toByteArray

    cipher foreach (cipher => cipher.decrypt(headerBytesArray))

    val decryptedBits = BitVector(headerBytesArray)

    Codec[ServerHeader].decode(decryptedBits) match {
      case Successful(DecodeResult(header, BitVector.empty)) =>
        val payloadBits = bits.drop(headerLength)

        (header, payloadBits)
      case Successful(DecodeResult(_, remainder)) =>
        throw PacketPartialReadException(remainder)
      case Failure(cause) =>
        throw MalformedPacketHeaderException(cause)
    }
  }

}
