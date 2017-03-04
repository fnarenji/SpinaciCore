package ensiwow.realm.protocol

import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._

/**
  * Represents the header of a packet sent by the server
  *
  * @param opCode      op code of packet
  * @param payloadSize size of payload (excluding header size)
  */
case class ServerPacketHeader(payloadSize: Int, opCode: OpCodes.Value) {
  require(payloadSize >= 0)
}

object ServerPacketHeader {
  val OpCodeSize: Int = 16

  implicit val codec: Codec[ServerPacketHeader] = {
    ("payloadSize" | integerOffset(serverPacketSizeCodec, - OpCodeSize / 8)) ::
      ("opCode" | enumerated(uintL(OpCodeSize), OpCodes))
  }.as[ServerPacketHeader]
}
