package wow.realm.objects

import scodec.Codec
import scodec.codecs._

/**
  * Entity types
  */
object ObjectType extends Enumeration {
  implicit lazy val codec: Codec[Value] = enumerated(uint8L, this)

  val Object = Value(0)
  val Item = Value(1)
  val Container = Value(2)
  val Unit = Value(3)
  val Player = Value(4)
  val GameObject = Value(5)
  val DynamicObject = Value(6)
  val Corpse = Value(7)
}
