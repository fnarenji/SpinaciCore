package ensiwow.realm.session

import java.io.IOException

import scodec.Err
import scodec.bits.BitVector

/**
  * Errors
  */
case class MalformedPacketHeaderException(err: Err) extends IOException(s"Malformed packet header: $err")

case class MalformedPacketException(err: Err) extends IOException(s"Malformed packet: $err")

case class PacketPartialReadException(remainder: BitVector) extends IOException(s"Invalid packet partial read: " +
  s"$remainder")

case class PacketSerializationException(err: Err) extends IOException(s"Packet couldn't be written: $err")

