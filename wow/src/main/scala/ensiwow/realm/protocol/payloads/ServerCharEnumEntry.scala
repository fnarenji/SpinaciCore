package ensiwow.realm.protocol.payloads

import ensiwow.realm.protocol.{OpCodeProvider, OpCodes, Payload, ServerHeader}
import ensiwow.common.codecs._
import scodec.Codec
import scodec.codecs._
import scodec._

import scala.language.postfixOps

/**
  * Packet storing all the characters to be sent to the client
  * @param characters a vector of characters
  */
case class ServerCharEnum(characters: Vector[ServerCharEnumEntry]) extends Payload[ServerHeader]

object ServerCharEnum {
  implicit val opCodeProvider: OpCodeProvider[ServerCharEnum] = OpCodes.SCharEnum

  implicit val codec: Codec[ServerCharEnum] = {
    variableSizeBytes(
      uint8L,
        "characters" | variableSizeVector(uint8L, Codec[ServerCharEnumEntry]))
  }.as[ServerCharEnum]
}

case class ServerCharEnumEntry(guid: Long,
                               charInfo: CharInfo,
                               level: Int,
                               zone: Long,
                               map: Long,
                               x: Float,
                               y: Float,
                               z: Float,
                               guildId: Long,
                               charFlag: Long,
                               charCustomFlag: Long,
                               atLogin: Int,
                               pet: Pet
                               // Inventory bag supposed always empty
                              )

object ServerCharEnumEntry {
  implicit val codec: Codec[ServerCharEnumEntry] = {
    ("guid"| uint32L) ::
      ("charInfo"| Codec[CharInfo]) ::
      ("level"| uint8L) ::
      ("zone"| uint32L) ::
      ("map"| uint32L) ::
      ("x"| float) ::
      ("y"| float) ::
      ("z"| float) ::
      ("guildId"| uint32L) ::
      ("charFlag"| uint32L) ::
      ("charCustomFlag"| uint32L) ::
      ("atLogin"| uint8L) ::
      ("pet"| Codec[Pet])
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
