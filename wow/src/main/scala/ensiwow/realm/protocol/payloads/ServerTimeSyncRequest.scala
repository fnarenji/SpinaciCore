package ensiwow.realm.protocol.payloads

import ensiwow.realm.protocol._
import scodec.Codec
import scodec.codecs._

/**
  * Server time sync request
  */
case class ServerTimeSyncRequest(number: Long) extends Payload with ServerSide {
  require(number >= 0)
}

object ServerTimeSyncRequest {
  implicit val opCodeProvider: OpCodeProvider[ServerTimeSyncRequest] = OpCodes.STimeSyncRequest

  implicit val codec: Codec[ServerTimeSyncRequest] = ("number" | uint32L).as[ServerTimeSyncRequest]
}
