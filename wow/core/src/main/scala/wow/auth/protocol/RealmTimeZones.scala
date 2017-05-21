package wow.auth.protocol

import pureconfig._
import wow.common.config._

/**
  * Time zones enumeration for realms
  */
object RealmTimeZones extends Enumeration with ConfigurationSerializableEnumeration {
  implicit val ec: ConfigConvert[RealmTimeZones.Value] = deriveEnumValue(this)

  val Development = Value(1)
  val UnitedStates = Value(2)
  val Oceanic = Value(3)
  val LatinAmerica = Value(4)
  val Tournament1 = Value(5)
  val Korea = Value(6)
  val Tournament2 = Value(7)
  val English = Value(8)
  val German = Value(9)
  val French = Value(10)
  val Spanish = Value(11)
  val Russian = Value(12)
  val Tournament3 = Value(13)
  val Taiwan = Value(14)
  val Tournament4 = Value(15)
  val China = Value(16)
  val CN1 = Value(17)
  val CN2 = Value(18)
  val CN3 = Value(19)
  val CN4 = Value(20)
  val CN5 = Value(21)
  val CN6 = Value(22)
  val CN7 = Value(23)
  val CN8 = Value(24)
  val Tournament5 = Value(25)
  val TestServer = Value(26)
  val Tournament6 = Value(27)
  val QAServer = Value(28)
  val CN9 = Value(29)
}
