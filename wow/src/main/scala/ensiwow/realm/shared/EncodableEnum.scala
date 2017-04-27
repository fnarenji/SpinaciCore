package ensiwow.realm.shared

import scodec._
import scodec.codecs._

/**
  * Abstract class implementing a generic codec
  *
  * @param valueCodec The existing codec from which the method should create the new codec
  * @param numeric type T's arithmetic
  * @tparam T the generic type
  */
abstract class EncodableEnum[T](valueCodec: Codec[T])(implicit numeric: Numeric[T]) extends Enumeration {
  lazy val codec: Codec[Value] =
    scodec.codecs.mappedEnum(valueCodec, this.values.map(e => e -> numeric.fromInt(e.id)).toMap)
}

object Genders extends EncodableEnum(uint8L) {
  val Male = Value(0)
  val Female = Value(1)
  val None = Value(2)
}

object Races extends EncodableEnum(uint8L) {
  val Human = Value(1)
  val Orc = Value(2)
  val Dwarf = Value(3)
  val Nightelf = Value(4)
  val UndeadPlayer = Value(5)
  val Tauren = Value(6)
  val Gnome = Value(7)
  val Troll = Value(8)
  val Bloodelf = Value(10)
  val Draenei = Value(11)
}

object Classes extends EncodableEnum(uint8L) {
  val Warrior = Value(1)
  val Paladin = Value(2)
  val Hunter = Value(3)
  val Rogue = Value(4)
  val Priest = Value(5)
  val DeathKnight = Value(6)
  val Shaman = Value(7)
  val Mage = Value(8)
  val Warlock = Value(9)
  val Druid = Value(11)
}
