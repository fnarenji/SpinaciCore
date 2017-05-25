package wow.realm.protocol.payloads

import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
import wow.realm.objects.{Guid, Position}
import wow.realm.protocol.{ClientSide, Payload, ServerSide}

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
