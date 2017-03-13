package ensiwow.realm.protocol

import ensiwow.auth.utils.PacketSerializationException
import ensiwow.realm.crypto.SessionCipher
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

/**
  * Packet serialization for realm packets
  */
object PacketSerialization {
  /**
    * Serializes an outgoing packet
    *
    * @param payload        payload to serialize
    * @param cipher         optional encryption cipher to be used
    * @param payloadCodec   codec used to serialize payload
    * @param opCodeProvider opcode provider for payload type
    * @tparam T payload type
    * @return bits of packet containing (encrypted) header and serialized payload
    */
  def outgoing[T <: Payload[ServerHeader]](payload: T)
                                          (cipher: Option[SessionCipher])
                                          (implicit payloadCodec: Codec[T],
                                           opCodeProvider: OpCodeProvider[T]): BitVector = {
    payloadCodec.encode(payload) match {
      case Successful(payloadBits) =>
        val header = ServerHeader(payloadBits.bytes.intSize.get, opCodeProvider.opCode)

        Codec[ServerHeader].encode(header) match {
          case Successful(headerBits) =>
            val encryptedHeader = headerBits.toByteArray

            cipher foreach { cipher => cipher.encrypt(encryptedHeader) }

            BitVector(encryptedHeader) ++ payloadBits
          case Failure(err) => throw PacketSerializationException(err)
        }
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  /**
    * Deserializes an incoming packet header
    *
    * @param bits   bits of incoming packet
    * @param cipher optional encryption cipher to be used
    * @return tuple containing deserialized header and payload bits
    */
  def incomingHeader(bits: BitVector)(cipher: Option[SessionCipher]): (ClientHeader, BitVector) = {
    val headerLength = Codec[ClientHeader].sizeBound.exact.get

    val headerBytes = bits.take(headerLength).toByteArray

    cipher foreach (cipher => cipher.decrypt(headerBytes))

    val decryptedBits = BitVector(headerBytes)

    Codec[ClientHeader].decode(decryptedBits) match {
      case Successful(DecodeResult(header, BitVector.empty)) =>
        val payloadBits = bits.drop(headerLength)

        (header, payloadBits)
    }
  }
}
