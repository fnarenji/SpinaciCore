package ensiwow.realm.entities

import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._

/**
  * Position of an entity (immutable)
  */
case class Position(mapId: Option[Long], x: Float, y: Float, z: Float, orientation: Float)

object Position {
  val vector5DCodec: Codec[Position] = {
    ("mapId" | requiredOptional(uint32L)) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      ("orientation" | floatL)
  }.as[Position]

  val vector4DCodec: Codec[Position] = {
    fixed[Option[Long]](None) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      ("orientation" | floatL)
  }.as[Position]
}


