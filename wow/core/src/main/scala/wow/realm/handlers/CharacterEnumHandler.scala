package wow.realm.handlers

import wow.realm.entities.CharacterInfo
import wow.realm.protocol.payloads._
import wow.realm.protocol.{OpCodes, PayloadlessPacketHandler, PayloadlessPacketHandlerFactory}
import wow.realm.session.NetworkWorker

/**
  * Handles player's characters list requests
  */
class CharacterEnumHandler extends PayloadlessPacketHandler {

  /**
    * Completes the description of a character
    * @param char partial information of a character
    * @return packet chunk containing the full description of a character
    */
  def completeDescription(char: CharacterInfo): ServerCharacterEnumEntry = {
    ServerCharacterEnumEntry(guid = char.guid,
      characterDescription = char.description,
      level = 1,
      zone = 12,
      position = char.position,
      guildId = 0,
      charFlag = 0x02000000,
      charCustomFlag = 0,
      atLogin = 0,
      pet = Pet.None
    )
  }

  override protected def process(): Unit = {
    val charactersEnum = for (char <- CharacterInfo.getCharacters) yield completeDescription(char)

    sender ! NetworkWorker.EventOutgoing(ServerCharacterEnum(charactersEnum.toVector))
  }

}

object CharacterEnumHandler extends PayloadlessPacketHandlerFactory[CharacterEnumHandler](OpCodes.CharEnum)
