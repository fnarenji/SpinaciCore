package wow.realm.protocol.payloads

import wow.realm.entities.Guid
import wow.realm.protocol._
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

