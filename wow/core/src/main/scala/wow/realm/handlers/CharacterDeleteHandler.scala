package wow.realm.handlers

import wow.realm.entities.CharacterInfo
import wow.realm.protocol._
import wow.realm.protocol.payloads.{ClientCharacterDelete, ServerCharacterDelete}
import wow.realm.session.Session

/**
  * Handles characters suppression requests
  * If valid, the character will be removed from CharacterInfo's list
  */
object CharacterDeleteHandler extends PayloadHandler[Session, ClientCharacterDelete] {
  /**
    * Processes the client's suppression request
    *
    * @param payload it contains the identification of the targeted character
    */
  override protected def handle(header: ClientHeader, payload: ClientCharacterDelete)(ps: Session): Unit = {
    val response = if (CharacterInfo.exists(payload.guid)) {
      CharacterInfo.deleteCharacter(payload.guid)
      CharacterDeletionResults.Success
    } else {
      CharacterDeletionResults.Failure
    }

    ps.sendPayload(ServerCharacterDelete(response))
  }
}

