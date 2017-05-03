package ensiwow.realm.protocol.payloads

import ensiwow.realm.entities.{Guid, Position}
import ensiwow.realm.protocol.{ClientSide, Payload, ServerSide}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Created by sknz on 4/23/17.
  */
case class ClientMovement(guid: Guid, flags: Long, extraFlags: Int, time: Long, position: Position, bytes: ByteVector)
  extends Payload with ClientSide with ServerSide

object ClientMovement {
  implicit val codec: Codec[ClientMovement] = (
    ("guid" | Guid.packedCodec) ::
      ("flags" | uint32L) ::
      ("extraFlags" | uint16L) ::
      ("time" | uint32L) ::
      ("position" | Position.codecXYZO) ::
      ("bytes" | bytes)
    ).as[ClientMovement]
}
