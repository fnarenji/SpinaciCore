package ensiwow.realm.protocol.payloads

import ensiwow.realm.entities.Guid
import ensiwow.realm.protocol._
import scodec.Codec
import scodec.codecs._

/**
  * Client player login payload
  */
case class ClientPlayerLogin(guid: Guid) extends Payload with ClientSide

object ClientPlayerLogin {
  implicit val opCodeProvider: OpCodeProvider[ClientPlayerLogin] = OpCodes.PlayerLogin

  implicit val codec: Codec[ClientPlayerLogin] = ("guid" | Guid.codec).as[ClientPlayerLogin]
}

