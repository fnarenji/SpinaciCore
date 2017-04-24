package ensiwow.realm.protocol.payloads

import scodec.Codec
import scodec.codecs._

import scala.language.postfixOps

case class CharInfo(name: String,
                    race: Int,
                    charClass: Int,
                    gender: Int,
                    skin: Int,
                    face: Int,
                    hairStyle: Int,
                    hairColor: Int,
                    facialHair: Int)

case class CreateInfo(charInfo: CharInfo,
                      outfitId: Int)

object CharInfo {
  implicit val codec: Codec[CharInfo] = {
    ("name" | cstring) ::
      ("race" | uint8L) ::
      ("class" | uint8L) ::
      ("gender" | uint8L) ::
      ("skin" | uint8L) ::
      ("face" | uint8L) ::
      ("hairStyle" | uint8L) ::
      ("hairColor" | uint8L) ::
      ("facialHair" | uint8L)
  }.as[CharInfo]
}

object CreateInfo {
  val MaxNameLength: Int = 12

  implicit val codec: Codec[CreateInfo] = {
    ("charInfo" | Codec[CharInfo]) ::
      ("outfitId" | uint8L)
  }.as[CreateInfo]
}

