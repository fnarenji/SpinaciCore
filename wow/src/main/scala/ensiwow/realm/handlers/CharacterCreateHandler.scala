package ensiwow.realm.handlers

import ensiwow.realm.entities.{CharacterInfo, Position}
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory, ResponseCodes}
import ensiwow.realm.protocol.payloads.{CharacterDescription, ClientCharacterCreate, ServerCharacterCreate}
import ensiwow.realm.session.NetworkWorker

/**
  * Handles character creation requests
  * If the character is valid, it will be added to the CharacterInfo list
  */
class CharacterCreateHandler extends PayloadHandler[ClientCharacterCreate] {

  /**
    * Checks if the entered name is valid
    * @param name the name of the desired character
    * @return the response code of the response packet
    */
  def checkNameValidity(name: String): ResponseCodes.Value = {
    if (name.isEmpty || name.length >= CharacterDescription.MaxNameLength) {
      ResponseCodes.CharCreateFailed
    } else {
      ResponseCodes.CharCreateSuccess
    }
  }

  /**
    * Processes the creation request packet
    * @param payload the client's packet containing the information on the desired character
    */
  override def process(payload: ClientCharacterCreate): Unit = {
    val response = checkNameValidity(payload.character.charInfo.name)

    if (response == ResponseCodes.CharCreateSuccess) {
        CharacterInfo.addCharacter(
          CharacterInfo(CharacterInfo.getNextGuid,
          Position.mxyzo(0, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
          payload.character.charInfo))
    }

    sender ! NetworkWorker.EventOutgoing(ServerCharacterCreate(response))
  }
}

object CharacterCreateHandler extends PayloadHandlerFactory[CharacterCreateHandler, ClientCharacterCreate]
