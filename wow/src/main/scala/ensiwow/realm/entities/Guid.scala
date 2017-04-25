package ensiwow.realm.entities

import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._

/**
  * Represents a unique Guid attributed to an entity.
  */
case class Guid(id: Guid.Id, guidType: GuidType.Value) {
  require(id >= 0)
}

object Guid {
  type Id = Int


  val codec: Codec[Guid] = (
    ("guid" | uintL(24)) ::
      constantE(0)(uintL(24)) ::
      ("guidType" | enumerated(uintL(16), GuidType))
    ).as[Guid]

  val packedCodec: Codec[Guid] = zeroPacked(8, codec)
}
