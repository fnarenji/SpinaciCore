package wow.realm.entities

import scodec.bits.ByteVector

/**
  * Created by sknz on 5/19/17.
  */
object CharacterRef {
  def unapply(arg: CharacterRef): Option[(Guid, Position, ByteVector, ByteVector)] = Some((
    arg.guid,
    arg.position,
    arg.selfBytes,
    arg.otherBytes
  ))
}

class CharacterRef(private val characterInfo: CharacterInfo) {
  def guid: Guid = characterInfo.guid

  def position: Position = characterInfo.position

  def selfBytes: ByteVector = characterInfo.selfBytes

  def otherBytes: ByteVector = characterInfo.otherBytes
}