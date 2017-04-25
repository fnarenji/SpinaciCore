package ensiwow.realm.shared

import scodec._
import scodec.codecs._

abstract class EncodableEnum[T](valueCodec: Codec[T])(implicit numeric: Numeric[T]) extends Enumeration {
  private var _codec: Codec[Value] = _
  implicit def codec: Codec[Value] = {
    if (_codec == null) {
      _codec = scodec.codecs.mappedEnum(valueCodec, this.values.map(e => e -> numeric.fromInt(e.id)).toMap)
    }

    _codec
  }
}

object Genders extends EncodableEnum(uint8L) {
  val GenderMale   = Value(0)
  val GenderFemale = Value(1)
  val GenderNone   = Value(2)
}

object Races extends EncodableEnum(uint8L) {
  val RaceHuman          = Value(1)
  val RaceOrc            = Value(2)
  val RaceDwarf          = Value(3)
  val RaceNightelf       = Value(4)
  val RaceUndeadPlayer  = Value(5)
  val RaceTauren         = Value(6)
  val RaceGnome          = Value(7)
  val RaceTroll          = Value(8)
  val RaceBloodelf       = Value(10)
  val RaceDraenei        = Value(11)
}

object Classes extends EncodableEnum(uint8L) {
  val ClassWarrior       = Value(1)
  val ClassPaladin       = Value(2)
  val ClassHunter        = Value(3)
  val ClassRogue         = Value(4)
  val ClassPriest        = Value(5)
  val ClassDeathKnight  = Value(6)
  val ClassShaman        = Value(7)
  val ClassMage          = Value(8)
  val ClassWarlock       = Value(9)
  val ClassDruid         = Value(11)
}


