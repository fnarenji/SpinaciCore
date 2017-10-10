package wow.realm.objects.fields

import scodec.Codec
import scodec.codecs._
import wow.common.codecs.fixedBitmask

object TypeMask extends Enumeration {
  implicit lazy val codecValueSet: Codec[TypeMask.ValueSet] = fixedBitmask(uint32L, this)

  val Object         = Value(0x0001)
  val Item           = Value(0x0002)
  val Container      = Value(0x0006)
  val Unit           = Value(0x0008)
  val Player         = Value(0x0010)
  val GameObject     = Value(0x0020)
  val DynamicObject  = Value(0x0040)
  val Corpse         = Value(0x0080)
}











