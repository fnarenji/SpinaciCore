package ensiwow.realm.protocol

import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._

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

  implicit val codec: Codec[ServerHeader] = {
    ("payloadSize" | integerOffset(serverPacketSizeCodec, - OpCodeSize / 8)) ::
      ("opCode" | enumerated(uintL(OpCodeSize), OpCodes))
  }.as[ServerHeader]
}
