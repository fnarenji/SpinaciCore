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
case class ServerCharacterEnum(characters: Vector[ServerCharacterEnumEntry]) extends Payload with ServerSide

object ServerCharacterEnum {
  implicit val opCodeProvider: OpCodeProvider[ServerCharacterEnum] = OpCodes.SCharEnum

  implicit val codec: Codec[ServerCharacterEnum] = {
    "characters" | variableSizeVector(uint8L, Codec[ServerCharacterEnumEntry])
  }.as[ServerCharacterEnum]
}

case class ServerCharacterEnumEntry(guid: Guid,
                                    characterDescription: CharacterDescription,
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

object ServerCharacterEnumEntry {
  implicit val codec: Codec[ServerCharacterEnumEntry] = {
    val inventorySlotSize = 9
    val inventorySlotCount = 23
    ("guid" | Guid.codec) ::
      ("charInfo" | Codec[CharacterDescription]) ::
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
  }.as[ServerCharacterEnumEntry]
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
