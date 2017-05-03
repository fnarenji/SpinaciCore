package ensiwow.realm.protocol.payloads

import ensiwow.realm.entities.Guid
import ensiwow.realm.protocol._
import scodec.Codec
import scodec.codecs._

case class ClientCharacterDelete(guid: Guid) extends Payload with ClientSide

object ClientCharacterDelete {
  implicit val opCodeProvider: OpCodeProvider[ClientCharacterDelete] = OpCodes.CharDelete

  implicit val codec: Codec[ClientCharacterDelete] = ("guid" | Guid.codec).as[ClientCharacterDelete]

}

case class ServerCharacterDelete(responseCode: CharacterDeletionResults.Value) extends Payload with ServerSide

object ServerCharacterDelete {
  implicit val opCodeProvider: OpCodeProvider[ServerCharacterDelete] = OpCodes.SCharDelete

  implicit val codec: Codec[ServerCharacterDelete] =
    ("responseCode" | Codec[CharacterDeletionResults.Value]).as[ServerCharacterDelete]
}

