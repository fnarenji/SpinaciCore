package wow.client.realm

import scodec.Codec
import scodec.codecs._
import wow.auth.protocol.OpCodes
import wow.common.codecs._
import wow.realm.protocol.PacketHeader

/**
  * Represents the header of a packet sent by the server
  *
  * @param opCode      op code of packet
  * @param payloadSize size of payload (excluding opcode)
  */
case class ServerHeader(payloadSize: Int, opCode: OpCodes.Value) extends PacketHeader {
  require(payloadSize >= 0)
}

object ServerHeader {
  val OpCodeSize: Int = 16
  val MinSize: Int = 32
  val MaxSize: Int = 40


  implicit val codec: Codec[ServerHeader] = {
    // Note: this one is stored as big endian on the wire
    ("payloadSize" | integerOffset(serverPacketSizeCodec, - OpCodeSize / 8)) ::
      ("opCode" | enumerated(uintL(OpCodeSize), OpCodes))
  }.as[ServerHeader]
}
