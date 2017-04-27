package ensiwow.realm.entities

import ensiwow.realm.protocol.payloads.CharacterDescription
import ensiwow.realm.shared.{Classes, Genders, Races}
import scodec.bits._
import scodec.codecs._

import scala.collection.{GenIterable, parallel}
import scala.collection.parallel.mutable

/**
  * Game character info
  */
case class CharacterInfo(guid: Guid, selfBytes: ByteVector, otherBytes: ByteVector, description: CharacterDescription) {
  var position: Position = _

  val ref: CharacterRef = new CharacterRef(this)
}

class CharacterRef(characterInfo: CharacterInfo) {
  def guid: Guid = characterInfo.guid

  def position: Position = characterInfo.position

  def selfBytes: ByteVector = characterInfo.selfBytes

  def otherBytes: ByteVector = characterInfo.otherBytes
}

object CharacterRef {
  def unapply(arg: CharacterRef): Option[(Guid, Position, ByteVector, ByteVector)] = Some(
    arg.guid,
    arg.position,
    arg.selfBytes,
    arg.otherBytes
  )
}

object CharacterInfo {

  def apply(guid: Guid,
            position: Position,
            description: CharacterDescription): CharacterInfo = {
    val unitFieldSelf1 = hex"2A150080119500C0D8DF80F5010800004A06000406000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020E200100000000000000000000000000000000000FE0000000040000000000080000000003F000000"
    val unitFieldSelf2 = hex"00190000000000803F01040003370000006400000037000000E803000064000000E803000001000000010000000800000000080000D0070000D0070000D0070000022BC73E0000C03F31000000310000006EDB96406EDBB640000000000000803F0000000015000000170000001500000014000000140000002E000000190000001A0000000E00000000004040000080400000803F08080706040000029001000002000000C9659E41FCCB1C41FCCB1C41FCCB1C41000000200000803F0000803F0000803F0000803F0000803F0000803F0000803FFFFFFFFF5000000015000000160000001700000018000000190000001A000000"
    val id = uintL(24).encode(guid.id).require.toByteVector
    val unitFieldOther1 = hex"2A150080119500C0F81E800500000000000000040600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    val unitFieldOther2 = hex"00190000000000803F01040003370000006400000037000000E803000064000000E80300000100000001000000080000000008000000004000D0070000D0070000022BC73E0000C03F3100000031000000000000000000803F000000000000803F0100020305000002"

    val char = CharacterInfo(guid, unitFieldSelf1 ++ id ++ unitFieldSelf2, unitFieldOther1 ++ id ++ unitFieldOther2, description)

    char.position = position

    char
  }

  def addCharacter(character: CharacterInfo): Unit = charactersByGuid += character.guid -> character

  def deleteCharacter(guid: Guid): Unit = charactersByGuid -= guid

  def getCharacters: GenIterable[CharacterInfo] = charactersByGuid.values

  def exists(guid: Guid): Boolean = charactersByGuid.contains(guid)

  private val charactersByGuid: mutable.ParMap[Guid, CharacterInfo] = parallel.mutable.ParHashMap(
    Seq(
      CharacterInfo(Guid(8, GuidType.Player),
        Position.mxyzo(0, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
        CharacterDescription(
          "VagueNerf",
          race = Races.Human,
          charClass = Classes.Rogue,
          gender = Genders.Male,
          skin = 8,
          face = 8,
          hairStyle = 7,
          hairColor = 6,
          facialHair = 4
        )
      ),
      CharacterInfo(Guid(9, GuidType.Player),
        Position.mxyzo(0, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
        CharacterDescription(
          "BrocQue10",
          race = Races.Human,
          charClass = Classes.Rogue,
          gender = Genders.Male,
          skin = 0,
          face = 6,
          hairStyle = 6,
          hairColor = 0,
          facialHair = 6
        )
      )
    ).map(c => c.guid -> c): _*
  )

  def byGuid(guid: Guid): CharacterInfo = charactersByGuid(guid)

  def getNextGuid: Guid = Guid(charactersByGuid.keys.max.id + 1, GuidType.Player)

}


