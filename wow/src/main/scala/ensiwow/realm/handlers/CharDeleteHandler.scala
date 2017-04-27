package ensiwow.realm.handlers

import ensiwow.realm.entities.CharacterInfo
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory, ResponseCodes}
import ensiwow.realm.protocol.payloads.{ClientCharacterDelete, ServerCharacterDelete}
import ensiwow.realm.session.NetworkWorker

/**
  * Handles characters suppression requests
  * If valid, the character will be removed from CharacterInfo's list
  */
class CharDeleteHandler extends PayloadHandler[ClientCharacterDelete] {

  /**
    * Processes the client's suppression request
    * @param payload it contains the identification of the targeted character
    */
  override protected def process(payload: ClientCharacterDelete): Unit = {
    val response = if (CharacterInfo.exists(payload.guid)) {
      ResponseCodes.CharDeleteSuccess
    } else {
      CharacterInfo.deleteCharacter(payload.guid)
      ResponseCodes.CharDeleteFailure
    }

    sender ! NetworkWorker.EventOutgoing(ServerCharacterDelete(response))
  }
}

object CharDeleteHandler extends PayloadHandlerFactory[CharDeleteHandler, ClientCharacterDelete]
