package ensiwow.realm.protocol

import ensiwow.Application
import ensiwow.realm.entities.{CharacterView, EntityType}
import ensiwow.realm.protocol.objectupdates.{UpdateFlags, UpdateType}
import ensiwow.realm.protocol.payloads.{MoveSpeeds, MovementInfo, ServerUpdateBlock}

/**
  * Object update packet blocks helper
  */
object ObjectUpdateBlockHelpers {
  def createCharacter(character: CharacterView, isSelf: Boolean): ServerUpdateBlock = character match {
    case CharacterView(guid, position, selfBytes, otherBytes) =>
      var updateFlags = UpdateFlags.Living + UpdateFlags.StationaryPosition

      val valueBytes = if (isSelf) {
        selfBytes
      } else {
        otherBytes
      }

      if (isSelf) {
        updateFlags += UpdateFlags.Self
      }

      val block = ServerUpdateBlock(
        UpdateType.CreateObject2,
        guid,
        EntityType.Player,
        MovementInfo(
          updateFlags,
          0,
          0,
          Application.uptimeMillis(),
          position,
          0
        ),
        MoveSpeeds.FastSpeeds,
        valueBytes
      )

      block
  }
}

