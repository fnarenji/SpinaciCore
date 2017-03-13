package ensiwow.auth.utils

import java.io.IOException

import ensiwow.auth.protocol.{ClientPacket, ServerPacket}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult, Err}

/**
  * Errors
  */
case class MalformedPacketHeaderException(err: Err) extends IOException(s"Malformed packet header: $err")

case class MalformedPacketException(err: Err) extends IOException(s"Malformed packet: $err")

case class PacketPartialReadException(remainder: BitVector) extends IOException(s"Invalid packet partial read: " +
  s"$remainder")

case class PacketSerializationException(err: Err) extends IOException(s"Packet couldn't be written: $err")

object PacketSerializer {
  /**
    * Serializes a packet of type T to a bitvector
    *
    * @param value packet to be serialized
    * @param codec codec
    * @tparam T packet type
    * @return bit vector containing serialized object
    */
  def serialize[T <: ServerPacket](value: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(value) match {
      case Successful(bits) => bits
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  /**
    * Deserializes a packet of type T from the bitvector
    *
    * @param bits  bitvector from which to read
    * @param codec codec used for deserialization
    * @tparam T type of packet
    * @return deserialized packets
    */
  def deserialize[T <: ClientPacket](bits: BitVector)(implicit codec: Codec[T]): T = {
    codec.decode(bits) match {
      case Successful(DecodeResult(value, BitVector.empty)) => value
      case Successful(DecodeResult(_, remainder)) => throw PacketPartialReadException(remainder)
      case Failure(err) => throw MalformedPacketException(err)
    }
  }
}

