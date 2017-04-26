package ensiwow.realm.protocol

import ensiwow.realm.shared.EncodableEnum
import scodec._
import scodec.codecs._

object ResponseCodes extends EncodableEnum(uint8L) {
  // implicit val codec: Codec[ResponseCodes.Value] = enumerated(uint8L, ResponseCodes)

  val CharCreateSuccess = Value(47)
  val CharCreateError = Value(48)
  val CharCreateFailed = Value(49)

  val CharNameFailure = Value(88)
  val CharNameNoName  = Value(89)

  val CharDeleteSuccess = Value(71)
  val CharDeleteFailure = Value(72)
}
