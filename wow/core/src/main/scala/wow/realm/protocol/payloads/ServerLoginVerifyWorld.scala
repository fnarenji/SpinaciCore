package wow.realm.protocol.payloads

import wow.realm.entities.Position
import wow.realm.protocol._
import scodec.Codec
import scodec.codecs._

/**
  * Created by sknz on 3/24/17.
  */
case class ServerLoginVerifyWorld(playerPosition: Position) extends Payload with ServerSide {
  require(playerPosition != null)
}

object ServerLoginVerifyWorld {
  implicit val opCodeProvider: OpCodeProvider[ServerLoginVerifyWorld] = OpCodes.SLoginVerifyWorld
  implicit val codec: Codec[ServerLoginVerifyWorld] = ("playerPosition" | Position.codecMXYZO).as[ServerLoginVerifyWorld]
}

