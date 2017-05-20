package wow.client.realm

import scodec.Codec
import scodec.codecs._
import wow.auth.protocol.OpCodes
import wow.common.codecs._
import wow.realm.protocol.PacketHeader

/**
  * Represents the header of a packet sent by the client
  *
  * @param opCode      op code of packet
  * @param payloadSize size of payload (excluding opcode)
  */
case class ClientHeader(payloadSize: Int, opCode: OpCodes.Value) extends PacketHeader {
  require(payloadSize >= 0)
  require(payloadSize < ClientHeader.MaxPayloadSize)
}

object ClientHeader {
  val OpCodeSize: Int = 32
  val MaxPayloadSize: Int = 10240 - OpCodeSize / 8

  implicit val codec: Codec[ClientHeader] = {
    // Note: this one is stored as big endian on the wire
    ("payloadSize" | integerOffset(uint16, - OpCodeSize / 8)) ::
      ("opCode" | enumerated(uintL(OpCodeSize - 1), OpCodes)) ::
      ignore(1)
  }.as[ClientHeader]
}
