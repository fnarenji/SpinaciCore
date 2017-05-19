package wow.realm.protocol.payloads

import wow.common.codecs._
import wow.realm.entities.{EntityType, Guid, Position}
import wow.realm.protocol.objectupdates.{UpdateFlags, UpdateType}
import wow.realm.protocol._
import scodec.Codec
import scodec.bits._
import scodec.codecs._
import scala.collection.immutable

/**
  * Object update payload
  */
case class ServerUpdateObject(blocks: immutable.Seq[ServerUpdateBlock]) extends Payload with ServerSide

object ServerUpdateObject {
  implicit val opCodeProvider: OpCodeProvider[ServerUpdateObject] = OpCodes.SUpdateObject

  implicit val codec: Codec[ServerUpdateObject] =
    ("blocks" | sizePrefixedSeq(long2Int(uint32L), ServerUpdateBlock.codec)).as[ServerUpdateObject]
}

case class MoveSpeeds(
  walk: Float,
  run: Float,
  runBackwards: Float,
  swim: Float,
  swimBackwards: Float,
  flight: Float,
  flightBackwards: Float,
  turnRate: Float,
  pitchRate: Float
)

object MoveSpeeds {
  implicit val codec: Codec[MoveSpeeds] = (
    ("walk" | floatL) ::
      ("run" | floatL) ::
      ("runBackwards" | floatL) ::
      ("swim" | floatL) ::
      ("swimBackwards" | floatL) ::
      ("flight" | floatL) ::
      ("flightBackwards" | floatL) ::
      ("turnRate" | floatL) ::
      ("pitchRate" | floatL)
    ).as[MoveSpeeds]

  val DefaultSpeeds = MoveSpeeds(
    walk = 2.5f,
    run = 7.0f,
    runBackwards = 4.5f,
    swim = 4.722222f,
    swimBackwards = 2.5f,
    flight = 7.0f,
    flightBackwards = 4.5f,
    turnRate = 3.141594f,
    pitchRate = 3.14f
  )

  val FastSpeeds = MoveSpeeds(
    walk = 2.5f,
    run = 21.0f,
    runBackwards = 4.5f,
    swim = 4.722222f,
    swimBackwards = 2.5f,
    flight = 7.0f,
    flightBackwards = 4.5f,
    turnRate = 3.141594f,
    pitchRate = 3.14f
  )
}

case class MovementInfo(
  updateFlags: UpdateFlags.ValueSet,
  movementFlags: Long,
  extraMovementFlags: Int,
  msTime: Long,
  position: Position,
  fallTime: Long
)

object MovementInfo {
  implicit val codec: Codec[MovementInfo] = {
    ("updateFlags" | fixedBitmask(uint16L, UpdateFlags)) ::
      ("movementFlags" | uint32L) ::
      ("extraMovementFlags" | uint16L) ::
      ("msTime" | uint32L) ::
      ("position" | Position.codecXYZO) ::
      ("fallTime" | uint32L)
  }.as[MovementInfo]
}

case class ServerUpdateBlock(
  updateType: UpdateType.Value,
  guid: Guid,
  entityType: EntityType.Value,
  movementInfo: MovementInfo,
  moveSpeeds: MoveSpeeds,
  fields: ByteVector
)

object ServerUpdateBlock {
  implicit val codec: Codec[ServerUpdateBlock] = (
    ("updateType" | Codec[UpdateType.Value]) ::
      ("guid" | Guid.packedCodec) ::
      ("entityType" | Codec[EntityType.Value]) ::
      ("movementInfo" | Codec[MovementInfo]) ::
      ("moveSpeeds" | Codec[MoveSpeeds]) ::
      ("fields" | bytes)
    ).as[ServerUpdateBlock]
}
