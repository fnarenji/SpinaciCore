package ensiwow.realm.protocol.objectupdates

/**
  * Update flags
  */
object UpdateFlags extends Enumeration {
  val None = Value(0x0000)
  val Self = Value(0x0001)
  val Transport = Value(0x0002)
  val HasTarget = Value(0x0004)
  val Unknown = Value(0x0008)
  val LowGuid = Value(0x0010)
  val Living = Value(0x0020)
  val StationaryPosition = Value(0x0040)
  val Vehicle = Value(0x0080)
  val Position = Value(0x0100)
  val Rotation = Value(0x0200)
}
