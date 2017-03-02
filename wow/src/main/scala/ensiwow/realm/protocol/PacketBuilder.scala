package ensiwow.realm.protocol

import ensiwow.utils.PacketSerializationException
import scodec.Attempt.{Failure, Successful}
import scodec.Codec
import scodec.bits.BitVector

/**
  * Packet builder for realm packets
  */
object PacketBuilder {
  def server[T <: Payload](payload: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(payload) match {
      case Successful(payloadBits) =>
        val header = ServerPacketHeader(payloadBits.bytes.intSize.get, payload.opCode)

        Codec[ServerPacketHeader].encode(header) match {
          case Successful(headerBits) =>
            headerBits ++ payloadBits
          case Failure(err) => throw PacketSerializationException(err)
        }
      case Failure(err) => throw PacketSerializationException(err)
    }
  }
}
