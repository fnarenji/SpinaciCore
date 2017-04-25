package ensiwow.realm.protocol.payloads

import ensiwow.common.codecs._
import ensiwow.realm.entities.{Guid, Position}
import ensiwow.realm.protocol._
import scodec.bits.ByteVector
import scodec.codecs._
import scodec.{Codec, _}

import scala.language.postfixOps

/**
  * Packet storing all the characters to be sent to the client
  *
  * @param characters a vector of characters
  */
case class ServerCharEnum(characters: Vector[ServerCharEnumEntry]) extends Payload with ServerSide

object ServerCharEnum {
  implicit val opCodeProvider: OpCodeProvider[ServerCharEnum] = OpCodes.SCharEnum

  implicit val codec: Codec[ServerCharEnum] = {
    "characters" | variableSizeVector(uint8L, Codec[ServerCharEnumEntry])
  }.as[ServerCharEnum]
}

case class ServerCharEnumEntry(
  guid: Guid,
  charInfo: CharInfo,
  level: Int,
  zone: Long,
  position: Position,
  guildId: Long,
  charFlag: Long,
  charCustomFlag: Long,
  atLogin: Int,
  pet: Pet
  // Inventory bag supposed always empty
)

object ServerCharEnumEntry {
  implicit val codec: Codec[ServerCharEnumEntry] = {
    val inventorySlotSize = 9
    val inventorySlotCount = 23
    ("guid" | Guid.codec) ::
      ("charInfo" | Codec[CharInfo]) ::
      ("level" | uint8L) ::
      ("zone" | uint32L) ::
      ("position" | Position.codecMXYZ) ::
      ("guildId" | uint32L) ::
      ("charFlag" | uint32L) ::
      ("charCustomFlag" | uint32L) ::
      ("atLogin" | uint8L) ::
      ("pet" | Codec[Pet]) ::
      constantE(ByteVector.low(inventorySlotCount * inventorySlotSize))(bytes)
    // Inventory bag supposed always empty
  }.as[ServerCharEnumEntry]
}

case class Pet(displayId: Long, level: Long, family: Long)

object Pet {
  implicit val codec: Codec[Pet] = {
    ("displayId" | uint32L) ::
      ("level" | uint32L) ::
      ("family" | uint32L)
  }.as[Pet]
}

case class ItemTemplate(displayInfoId: Long, inventoryType: Int, auraId: Long)

object ItemTemplate {
  implicit val codec: Codec[ItemTemplate] = {
    ("displayInfoId" | uint32L) ::
      ("inventoryType" | uint8L) ::
      ("auraId" | uint32L)
  }.as[ItemTemplate]
}
