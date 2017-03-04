package ensiwow.realm.protocol

import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._

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
  val OpCodeSize = 32
  val MaxPayloadSize = 10240 - OpCodeSize / 8

  implicit val codec: Codec[ClientHeader] = {
    ("payloadSize" | integerOffset(uint16L, - OpCodeSize / 8)) ::
      ("opCode" | enumerated(uintL(OpCodeSize - 1), OpCodes)) ::
      ignore(1)
  }.as[ClientHeader]
}

