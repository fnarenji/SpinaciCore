package ensiwow.realm.entities

import ensiwow.realm.shared.{EnumCodecProvider, NumericCodecTag}
import scodec.codecs._

/**
  * Entity types
  */
object EntityType extends Enumeration with EnumCodecProvider[Int] {
  override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

  val Object = Value(0)
  val Item = Value(1)
  val Container = Value(2)
  val Unit = Value(3)
  val Player = Value(4)
  val GameObject = Value(5)
  val DynamicObject = Value(6)
  val Corpse = Value(7)
}
