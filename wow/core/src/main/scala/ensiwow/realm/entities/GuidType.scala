package ensiwow.realm.entities

import ensiwow.realm.shared.{EnumCodecProvider, NumericCodecTag}
import scodec.codecs._

/**
  * Entity type part of Guid
  */
object GuidType extends Enumeration with EnumCodecProvider[Int] {
  override protected val valueCodecTag: NumericCodecTag[Int] = uintL(16)

  val Item = Value(0x4000)
  val Container = Item
  val Player = Value(0x0000)
  val GameObject = Value(0xF110)
  val Transport = Value(0xF120)
  val Unit = Value(0xF130)
  val Pet = Value(0xF140)
  val Vehicle = Value(0xF150)
  val DynamicObject = Value(0xF100)
  val Corpse = Value(0xF101)
  val MoTransport = Value(0x1FC0)
  val Instance = Value(0x1F40)
  val Group = Value(0x1F50)
}
