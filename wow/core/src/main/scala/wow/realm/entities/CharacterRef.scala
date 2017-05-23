package wow.realm.entities

import scodec.bits.ByteVector

/**
  * This a concurrentlly readable version of CharacterInfo.
  * Not much use right now, still need to think more about this.
  * Right now it is used as a way to fast access an entity's position (i.e. without using a message)
  */
class CharacterRef(private val characterInfo: CharacterInfo) {
  def guid: Guid = characterInfo.guid

  def position: Position = characterInfo.position

  def selfBytes: ByteVector = characterInfo.selfBytes

  def otherBytes: ByteVector = characterInfo.otherBytes
}

object CharacterRef {
  def unapply(arg: CharacterRef): Option[(Guid, Position, ByteVector, ByteVector)] = Some((
    arg.guid,
    arg.position,
    arg.selfBytes,
    arg.otherBytes
  ))
}

