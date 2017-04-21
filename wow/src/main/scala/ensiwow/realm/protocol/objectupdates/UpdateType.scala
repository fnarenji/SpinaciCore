package ensiwow.realm.protocol.objectupdates

import scodec._
import scodec.codecs._

/**
  * Object update type indicates nature of update (e.g. value change, movement, creation...)
  */
object UpdateType extends Enumeration {
  val Values = Value(0)
  val Movement = Value(1)
  val CreateObject = Value(2)
  val CreateObject2 = Value(3)
  val OutOfRangeObjects = Value(4)
  val NearObjects = Value(5)

  implicit val codec: Codec[UpdateType.Value] = enumerated(uint8L, UpdateType)
}
