package ensiwow.realm.protocol

import ensiwow.utils.{PacketPartialReadException, PacketSerializationException}
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult}
import scodec.bits.BitVector

/**
  * Packet serialization for realm packets
  */
object PacketSerialization {
  def server[T <: Payload[ServerHeader]](payload: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(payload) match {
      case Successful(payloadBits) =>
        val header = ServerHeader(payloadBits.bytes.intSize.get, payload.opCode)

        Codec[ServerHeader].encode(header) match {
          case Successful(headerBits) =>
            headerBits ++ payloadBits
          case Failure(err) => throw PacketSerializationException(err)
        }
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  def client[T <: Payload[ClientHeader]](bits: BitVector)(implicit payloadCodec: Codec[T]): T = {
    val codec = Codec[ClientHeader] ~ payloadCodec
    codec.decode(bits) match {
      case Successful(DecodeResult((_, payload), BitVector.empty)) =>
        payload
      case Successful(DecodeResult(_, remainder)) =>
        throw PacketPartialReadException(remainder)
      case Failure(err) =>
        throw PacketSerializationException(err)
    }
  }
}
