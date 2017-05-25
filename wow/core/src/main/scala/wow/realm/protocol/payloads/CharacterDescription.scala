package wow.realm.protocol.payloads

import scodec.Codec
import scodec.codecs._
import wow.common.database.databasecomponent
import wow.realm.objects.{Classes, Genders, Races}

/**
  * Physical aspects of a character
  */
@databasecomponent
case class CharacterDescription(
  name: String,
  race: Races.Value,
  clazz: Classes.Value,
  gender: Genders.Value,
  skin: Int,
  face: Int,
  hairStyle: Int,
  hairColor: Int,
  facialHair: Int) {
  // TODO: add requirements on skin/face etc to check value validity ?
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

