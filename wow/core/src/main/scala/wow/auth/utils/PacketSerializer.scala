package wow.auth.utils

import java.io.IOException

import wow.auth.protocol.{ClientPacket, ServerPacket}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult, Err}

import scala.reflect.ClassTag

/**
  * Errors
  */
case class MalformedPacketHeaderException(err: Err) extends IOException(s"Malformed packet header: $err")

case class MalformedPacketException(err: Err) extends IOException(s"Malformed packet: $err")

case class PacketPartialReadException(remainder: BitVector) extends IOException(s"Invalid packet partial read: " +
  s"$remainder")

case class PacketSerializationException[A: ClassTag](err: Err)
  extends IOException(s"Packet ${implicitly[ClassTag[A]].runtimeClass.getSimpleName} couldn't be written: $err")

object PacketSerializer {
  /**
    * Serializes a packet of type T to a bitvector
    *
    * @param value packet to be serialized
    * @param codec codec
    * @tparam A packet type
    * @return bit vector containing serialized object
    */
  def serialize[A <: ServerPacket](value: A)(implicit codec: Codec[A]): BitVector = {
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
    * @tparam A type of packet
    * @return deserialized packets
    */
  def deserialize[A <: ClientPacket](bits: BitVector)(implicit codec: Codec[A]): A = {
    codec.decode(bits) match {
      case Successful(DecodeResult(value, BitVector.empty)) => value
      case Successful(DecodeResult(_, remainder)) => throw PacketPartialReadException(remainder)
      case Failure(err) => throw MalformedPacketException(err)
    }
  }
}

