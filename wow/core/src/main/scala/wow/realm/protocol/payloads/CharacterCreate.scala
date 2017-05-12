package wow.realm.protocol.payloads

import scodec.Codec
import scodec.codecs._
import wow.common.codecs._
import wow.realm.protocol._

case class ClientCharacterCreate(character: ClientCharacterCreateEntry) extends Payload with ClientSide

object ClientCharacterCreate {
  implicit val opCodeProvider: OpCodeProvider[ClientCharacterCreate] = OpCodes.CharCreate

  implicit val codec: Codec[ClientCharacterCreate] = ("createInfo" | Codec[ClientCharacterCreateEntry])
    .as[ClientCharacterCreate]
}

case class ClientCharacterCreateEntry(charInfo: CharacterDescription)

object ClientCharacterCreateEntry {
  implicit val codec: Codec[ClientCharacterCreateEntry] = {
    ("charInfo" | Codec[CharacterDescription]) ::
      // outfitId unused
      constantE(0x0)(uint8L)
  }.as[ClientCharacterCreateEntry]
}

case class ServerCharacterCreate(responseCode: CharacterCreationResults.Value) extends Payload with ServerSide

object ServerCharacterCreate {
  implicit val opCodeProvider: OpCodeProvider[ServerCharacterCreate] = OpCodes.SCharCreate

  implicit val codec: Codec[ServerCharacterCreate] =
    ("responseCode" | Codec[CharacterCreationResults.Value]).as[ServerCharacterCreate]
}


