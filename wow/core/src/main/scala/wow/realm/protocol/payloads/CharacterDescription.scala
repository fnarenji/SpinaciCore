package wow.realm.protocol.payloads

import wow.realm.entities.{Classes, Genders, Races}
import scodec.Codec
import scodec.codecs._

case class CharacterDescription(name: String,
                                race: Races.Value,
                                charClass: Classes.Value,
                                gender: Genders.Value,
                                skin: Int,
                                face: Int,
                                hairStyle: Int,
                                hairColor: Int,
                                facialHair: Int) {
}

object CharacterDescription {
  val MaxNameLength: Int = 12

  implicit val codec: Codec[CharacterDescription] = {
    ("name" | cstring) ::
      ("race" | Races.codec) ::
      ("class" | Classes.codec) ::
      ("gender" | Genders.codec) ::
      ("skin" | uint8L) ::
      ("face" | uint8L) ::
      ("hairStyle" | uint8L) ::
      ("hairColor" | uint8L) ::
      ("facialHair" | uint8L)
  }.as[CharacterDescription]
}


