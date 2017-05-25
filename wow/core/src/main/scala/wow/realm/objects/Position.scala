package wow.realm.objects

import scodec.Codec
import scodec.codecs._
import wow.common.codecs._
import wow.common.database.databasecomponent

/**
  * Position of an entity (immutable)
  */
@databasecomponent
case class Position(
  mapId: Option[Long],
  x: Float,
  y: Float,
  z: Float,
  orientation: Option[Float]) {
  /**
    * Merges another position with the current one.
    * Values from current position are preferred.
    *
    * @param p other position
    * @return merged position
    */
  def merge(p: Position): Position = {
    Position(
      mapId.orElse(p.mapId),
      x,
      y,
      z,
      orientation.orElse(p.orientation)
    )
  }
}

object Position {
  def xyz(x: Float, y: Float, z: Float): Position = Position(None, z, y, z, None)

  def xyzo(x: Float, y: Float, z: Float, o: Float): Position = Position(None, x, y, z, Some(o))

  def mxyz(mapId: Long, x: Float, y: Float, z: Float): Position = Position(Some(mapId), x, y, z, None)

  def mxyzo(mapId: Long, x: Float, y: Float, z: Float, o: Float): Position = Position(Some(mapId), x, y, z, Some(o))

  val codecMXYZO: Codec[Position] = {
    ("mapId" | requiredOptional(uint32L)) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      ("orientation" | requiredOptional(floatL))
  }.as[Position]

  val codecMXYZ: Codec[Position] = {
    ("mapId" | requiredOptional(uint32L)) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      fixed[Option[Float]](None)
  }.as[Position]

  val codecXYZO: Codec[Position] = {
    fixed[Option[Long]](None) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      ("orientation" | requiredOptional(floatL))
  }.as[Position]

  val codecXYZ: Codec[Position] = {
    fixed[Option[Long]](None) ::
      ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL) ::
      fixed[Option[Float]](None)
  }.as[Position]
}


