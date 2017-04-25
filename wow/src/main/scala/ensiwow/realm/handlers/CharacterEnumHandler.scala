package ensiwow.realm.handlers

import ensiwow.realm.entities.{CharacterInfo, Guid, Position}
import ensiwow.realm.protocol.payloads._
import ensiwow.realm.protocol.{OpCodes, PayloadlessPacketHandler, PayloadlessPacketHandlerFactory}
import ensiwow.realm.session.NetworkWorker

/**
  * Created by sknz on 3/15/17.
  */
class CharacterEnumHandler extends PayloadlessPacketHandler {

  def complete(char: (Guid, CharacterInfo)): ServerCharacterEnumEntry = {
    ServerCharacterEnumEntry(guid = char._1,
      characterDescription = char._2.description,
      level = 1,
      zone = 12,
      position = char._2.position,
      guildId = char._1.id,
      charFlag = 0x02000000,
      charCustomFlag = 0,
      atLogin = 0,
      pet = Pet(0, 0, 0)
    )
  }

  /**
    * Processes empty payload
    */
  override protected def process: Unit = {
    val charactersEnum = {
      for (char <- CharacterInfo.getCharacters) yield complete(char)
    }

    sender ! NetworkWorker.EventOutgoing(ServerCharacterEnum(charactersEnum.toVector))
  }

}

object CharacterEnumHandler extends PayloadlessPacketHandlerFactory[CharacterEnumHandler](OpCodes.CharEnum)
