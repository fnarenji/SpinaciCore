package ensiwow.realm.handlers

import ensiwow.realm.entities.CharacterInfo
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory, ResponseCodes}
import ensiwow.realm.protocol.payloads.{ClientCharacterDelete, ServerCharacterDelete}
import ensiwow.realm.session.NetworkWorker

/**
  * Created by yanncolina on 14/04/17.
  */
class CharDeleteHandler extends PayloadHandler[ClientCharacterDelete] {

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  override protected def process(payload: ClientCharacterDelete): Unit = {
    val response = if (CharacterInfo.contains(payload.guid)) {
      ResponseCodes.CharDeleteSuccess
    } else {
      ResponseCodes.CharDeleteFailure
    }

    response match {
      case ResponseCodes.CharDeleteSuccess => CharacterInfo.deleteCharacter(payload.guid)
    }

    sender ! NetworkWorker.EventOutgoing(ServerCharacterDelete(response))
  }
}

object CharDeleteHandler extends PayloadHandlerFactory[CharDeleteHandler, ClientCharacterDelete]
