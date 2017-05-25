package wow.realm.handlers

import org.joda.time.DateTime
import wow.realm.objects.characters.CharacterDao
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
  override protected def handle(header: ClientHeader, payload: ClientCharacterDelete)(self: Session): Unit = {
    import self._

    val response = CharacterDao.findByGuid(payload.guid) match {
      case Some(character) =>
        CharacterDao.save(character.copy(deletedAt = Some(DateTime.now)))
        CharacterDeletionResults.Success
      case None =>
        CharacterDeletionResults.Failure
    }

    sendPayload(ServerCharacterDelete(response))
  }
}

