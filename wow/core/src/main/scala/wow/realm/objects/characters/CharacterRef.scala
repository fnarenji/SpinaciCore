package wow.realm.objects.characters

import scodec.bits.ByteVector
import wow.realm.objects.{Guid, Position}

/**
  * This a concurrentlly readable version of CharacterInfo.
  * Not much use right now, still need to think more about this.
  * Right now it is used as a way to fast access an entity's position (i.e. without using a message)
  */
class CharacterRef(private val character: Character) {
  def guid: Guid = character.guid

  def position: Position = character.position

  def selfBytes: ByteVector = character.selfBytes

  def otherBytes: ByteVector = character.otherBytes
}

object CharacterRef {
  def unapply(arg: CharacterRef): Option[(Guid, Position, ByteVector, ByteVector)] = Some((
    arg.guid,
    arg.position,
    arg.selfBytes,
    arg.otherBytes
  ))
}

