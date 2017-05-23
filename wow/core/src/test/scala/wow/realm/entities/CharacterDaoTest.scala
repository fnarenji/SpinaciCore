package wow.realm.entities

import java.sql.SQLException

import org.joda.time.DateTime
import org.scalatest.{DoNotDiscover, FlatSpec, Matchers}
import wow.auth.crypto.Srp6Identity
import wow.auth.data.Account
import wow.common.database.AutoRollbackAfterSuite
import wow.realm.protocol.payloads.CharacterDescription

/**
  * DAO test for CharacterInfo
  */
@DoNotDiscover
class CharacterDaoTest extends FlatSpec with Matchers with AutoRollbackAfterSuite {
  val CharacterDao = CharacterInfoDao(1)

  var accountId: Int = _

  override def setup(): Unit = {
    accountId = Account.create("t", Srp6Identity(BigInt(1), BigInt(1)))
  }

  val expectedPosition: Position = Position.mxyzo(1, 1f, 2f, 3f, 4f)
  val expectedDescription = CharacterDescription("Test", Races.Troll, Classes.Shaman, Genders.Male, 1, 2, 3, 4, 5)
  var expectedCharacter: CharacterInfo = _

  behavior of "CharacterDao"

  var guid: Guid = _
  it should "create a character" in {
    guid = CharacterDao.create(accountId, expectedDescription, expectedPosition)

    assert(guid.guidType == GuidType.Character)

    assert(guid.id >= 0)

    expectedCharacter = CharacterInfo(guid, accountId, expectedDescription, expectedPosition)
  }

  it should "fetch by guid" in {
    val maybeCharacter = CharacterDao.findByGuid(guid)

    assert(maybeCharacter.isDefined)

    val character = maybeCharacter.get

    assert(character === expectedCharacter)
  }

  it should "fetch by account" in {
    val secondDescription = expectedDescription.copy(name = "Test2")
    val secondGuid = CharacterDao.create(accountId, secondDescription, expectedPosition)

    val secondCharacter = expectedCharacter.copy(guid = secondGuid, description = secondDescription)

    val characters = CharacterDao.findByAccount(accountId)

    assert(characters.size == 2)

    assert(characters.head == expectedCharacter)
    assert(characters(1) == secondCharacter)
  }

  it should "soft delete a character" in {
    val deletedCharacter = expectedCharacter.copy(deletedAt = Some(DateTime.now))

    CharacterDao.save(deletedCharacter)

    val character = CharacterDao.findByGuid(deletedCharacter.guid)
    assert(character.isEmpty)

    val characters = CharacterDao.findByAccount(accountId)
    assert(!characters.exists(c => c.guid == deletedCharacter.guid))
  }

  it should "save a character" in {
    val updatedDescription = expectedDescription.copy(facialHair = 3, race = Races.Tauren)
    val updatedCharacter = expectedCharacter.copy(
      description = updatedDescription,
      _position = Position.mxyzo(1, 5f, 6f, 7f, 8f)
    )

    CharacterDao.save(updatedCharacter)

    val maybeCharacter = CharacterDao.findByGuid(updatedCharacter.guid)

    assert(maybeCharacter.isDefined)

    val character = maybeCharacter.get

    assert(character === updatedCharacter)
  }

  it should "enforce name uniqueness" in {
    assertThrows[SQLException](CharacterDao.create(accountId, expectedDescription, expectedPosition))
  }
}

