package wow.realm.handlers

import wow.realm.entities.CharacterInfo
import wow.realm.protocol.payloads._
import wow.realm.protocol._
import wow.realm.session.Session

/**
  * Handles player's characters list requests
  */
object CharacterEnumHandler extends IgnorePayloadHandler[Session] {
  /**
    * Completes the description of a character
    *
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

  override def handle(header: ClientHeader)(self: Session): Unit = {
    import self._

    val charactersEnum = for (char <- CharacterInfo.getCharacters) yield {
      completeDescription(char)
    }

    sendPayload(ServerCharacterEnum(charactersEnum.toVector))
  }

  override val opCodes: OpCodes.ValueSet = OpCodes.ValueSet(OpCodes.CharEnum)
}

