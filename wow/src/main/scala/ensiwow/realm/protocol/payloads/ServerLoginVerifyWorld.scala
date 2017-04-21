package ensiwow.realm.protocol.payloads

import ensiwow.realm.entities.Position
import ensiwow.realm.protocol._
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
  implicit val codec: Codec[ServerLoginVerifyWorld] = ("playerPosition" | Position.vector5DCodec).as[ServerLoginVerifyWorld]
}

