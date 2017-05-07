package wow.realm.handlers

import wow.realm.entities.CharacterInfo
import wow.realm.protocol.{CharacterDeletionResults, PayloadHandler, PayloadHandlerFactory}
import wow.realm.protocol.payloads.{ClientCharacterDelete, ServerCharacterDelete}
import wow.realm.session.NetworkWorker

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
      CharacterInfo.deleteCharacter(payload.guid)
      CharacterDeletionResults.Success
    } else {
      CharacterDeletionResults.Failure
    }

    sender ! NetworkWorker.EventOutgoing(ServerCharacterDelete(response))
  }
}

object CharDeleteHandler extends PayloadHandlerFactory[CharDeleteHandler, ClientCharacterDelete]
