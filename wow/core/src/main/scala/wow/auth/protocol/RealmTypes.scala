package wow.auth.protocol

import pureconfig.ConfigConvert
import wow.common.config._

/**
  * Realm types
  */
object RealmTypes extends Enumeration with ConfigConvertible {
  implicit val ec: ConfigConvert[RealmTypes.Value] = deriveEnumValue(this)

  val Normal       = Value(0)
  val Pvp          = Value(1)
  val Normal2      = Value(4)
  val Rp           = Value(6)
  val RpPvp        = Value(8)
}
