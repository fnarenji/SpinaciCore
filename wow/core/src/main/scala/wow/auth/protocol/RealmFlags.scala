package wow.auth.protocol

import wow.common.config.ConfigConvertible

/**
  * Realm flags
  */
object RealmFlags extends Enumeration with ConfigConvertible {
  val None = Value(0x00)
  val VersionMismatch = Value(0x01)
  val Offline = Value(0x02)
  val SpecifyBuild = Value(0x04)
  val Medium = Value(0x08)
  val Medium2 = Value(0x10)
  val Recommended = Value(0x20)
  val New = Value(0x40)
  val Full = Value(0x80)
}

