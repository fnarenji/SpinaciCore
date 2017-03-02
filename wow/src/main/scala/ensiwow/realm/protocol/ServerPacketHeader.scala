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
  implicit val codec: Codec[ServerPacketHeader] = {
    ("payloadSize" | serverPacketSize) ::
      ("opCode" | Codec[OpCodes.Value])
  }.as[ServerPacketHeader]
}
