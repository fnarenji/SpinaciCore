package wow.realm.entities

import org.joda.time.DateTime
import scalikejdbc._
import scodec.bits._
import scodec.codecs._
import wow.common.database.{RichColumn, _}
import wow.realm.protocol.payloads.CharacterDescription

/**
  * Character information
  *
  * @param guid        character guid
  * @param accountId   owner account id
  * @param description character caracteristics
  * @param _position   character position
  * @param deletedAt   when was character deleted
  */
case class CharacterInfo(
  guid: Guid,
  accountId: Int,
  description: CharacterDescription,
  private var _position: Position,
  deletedAt: Option[DateTime] = None) {
  def position: Position = _position

  def position_=(newPosition: Position): Unit = {
    _position = newPosition.merge(_position)
  }

  val ref: CharacterRef = new CharacterRef(this)

  private val unitFieldSelf1: ByteVector =
    hex"2A150080119500C0D8DF80F5010800004A06000406000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020E200100000000000000000000000000000000000FE0000000040000000000080000000003F000000"
  private val unitFieldSelf2: ByteVector =
    hex"00190000000000803F01040003370000006400000037000000E803000064000000E803000001000000010000000800000000080000D0070000D0070000D0070000022BC73E0000C03F31000000310000006EDB96406EDBB640000000000000803F0000000015000000170000001500000014000000140000002E000000190000001A0000000E00000000004040000080400000803F08080706040000029001000002000000C9659E41FCCB1C41FCCB1C41FCCB1C41000000200000803F0000803F0000803F0000803F0000803F0000803F0000803FFFFFFFFF5000000015000000160000001700000018000000190000001A000000"
  private val id: ByteVector = uintL(24).encode(guid.id).require.toByteVector
  private val unitFieldOther1: ByteVector =
    hex"2A150080119500C0F81E800500000000000000040600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
  private val unitFieldOther2: ByteVector =
    hex"00190000000000803F01040003370000006400000037000000E803000064000000E80300000100000001000000080000000008000000004000D0070000D0070000022BC73E0000C03F3100000031000000000000000000803F000000000000803F0100020305000002"

  val selfBytes: ByteVector = unitFieldSelf1 ++ id ++ unitFieldSelf2
  val otherBytes: ByteVector = unitFieldOther1 ++ id ++ unitFieldOther2
}

/**
  * DAO of CharacterInfo. Access using shortcut [[wow.realm.entities.CharacterDAO]], as it per-realm.
  *
  * @param realmId realm id
  */
case class CharacterInfoDao(realmId: Int) extends SQLSyntaxSupport[CharacterInfo] with RichColumn[CharacterInfo] {
  override def tableName: String = "character_info"

  override def connectionPoolName: Any = Databases.RealmServer(realmId)

  /**
    * Type binder used for Guid to Int conversion
    */
  private implicit val guidBinder = Guid.typeBinder(GuidType.Character)

  def apply(s: SyntaxProvider[CharacterInfo])(rs: WrappedResultSet): CharacterInfo = apply(s.resultName)(rs)

  def apply(rn: ResultName[CharacterInfo])(rs: WrappedResultSet): CharacterInfo = {
    CharacterInfo(
      guid = rs.get[Guid](c.guid),
      accountId = rs.int(c.accountId),
      description = CharacterDescription(
        name = rs.string(c.name),
        race = rs.get[Races.Value](c.race),
        clazz = rs.get[Classes.Value](c.clazz),
        gender = rs.get[Genders.Value](c.gender),
        skin = rs.int(c.skin),
        face = rs.int(c.face),
        hairStyle = rs.int(c.hairStyle),
        hairColor = rs.int(c.hairColor),
        facialHair = rs.int(c.facialHair)
      ),
      _position = Position(
        mapId = rs.longOpt(c.mapId),
        x = rs.float(c.x),
        y = rs.float(c.y),
        z = rs.float(c.z),
        orientation = rs.floatOpt(c.orientation)
      ),
      deletedAt = rs.jodaDateTimeOpt(c.deletedAt)
    )
  }

  /**
    * Create a character bound to an account
    *
    * @param accountId   account id
    * @param description character caracteristics
    * @param position    initial position of character
    * @param session     database session
    * @return guid of character
    */
  def create(accountId: Int, description: CharacterDescription, position: Position)
    (implicit session: DBSession = autoSession): Guid = {
    val guid = withSQL {
      insert.into(this)
        .namedValues(
          c.accountId -> accountId,
          c.name -> description.name,
          c.race -> description.race.asParameterBinder,
          c.clazz -> description.clazz.asParameterBinder,
          c.gender -> description.gender.asParameterBinder,
          c.skin -> description.skin,
          c.face -> description.face,
          c.hairStyle -> description.hairStyle,
          c.hairColor -> description.hairColor,
          c.facialHair -> description.facialHair,
          c.mapId -> position.mapId,
          c.x -> position.x,
          c.y -> position.y,
          c.z -> position.z,
          c.orientation -> position.orientation
        )
    }.updateAndReturnGeneratedKey.apply().toInt

    Guid(guid, GuidType.Character)
  }

  /**
    * Saves character to database
    *
    * @param character character to save
    * @param session   database session
    */
  def save(character: CharacterInfo)(implicit session: DBSession = autoSession): Unit = assert(withSQL {
    update(this)
      .set(
        c.accountId -> character.accountId,
        c.name -> character.description.name,
        c.race -> character.description.race.asParameterBinder,
        c.clazz -> character.description.clazz.asParameterBinder,
        c.gender -> character.description.gender.asParameterBinder,
        c.skin -> character.description.skin,
        c.face -> character.description.face,
        c.hairStyle -> character.description.hairStyle,
        c.hairColor -> character.description.hairColor,
        c.facialHair -> character.description.facialHair,
        c.mapId -> character.position.mapId,
        c.x -> character.position.x,
        c.y -> character.position.y,
        c.z -> character.position.z,
        c.orientation -> character.position.orientation,
        c.deletedAt -> character.deletedAt
      )
      .where
      .eq(c.guid, character.guid.id)
  }.update().apply() > 0)

  /**
    * Finds one character matching a condition.
    * Deleted characters are excluded.
    *
    * @param condition condition to be matched
    * @return found character if exists
    */
  private def find(condition: SQLSyntax): SQL[CharacterInfo, HasExtractor] =
    withSQL {
      select(column.*)
        .from(this as syntax)
        .where
        .isNull(c.deletedAt)
        .and
        .append(condition)
    }.map(this (syntax))

  /**
    * Finds character by guid.
    * Deleted characters are excluded.
    *
    * @param guid    guid of character
    * @param session database session
    * @return found character if exists
    */
  def findByGuid(guid: Guid)(implicit session: DBSession = autoSession): Option[CharacterInfo] =
    find(sqls.eq(c.guid, guid)).single().apply()


  /**
    * Finds characters by account.
    * Deleted characters are excluded.
    *
    * @param accountId id of account
    * @param session   database session
    * @return collection of characters
    */
  def findByAccount(accountId: Int)(implicit session: DBSession = autoSession): Seq[CharacterInfo] =
    find(sqls.eq(c.accountId, accountId)).collection.apply()

  /**
    * Counts the number of characters satisfying a condition.
    *
    * @param condition condition
    * @param session   database session
    * @return number of characters meeting condition
    */
  def count(condition: Option[SQLSyntax] = None)(implicit session: DBSession = autoSession): Int = withSQL {
    select(sqls.count(column.asterisk))
      .from(this as syntax)
      .where(
        sqls.toAndConditionOpt(
          Some(sqls.isNull(c.deletedAt)),
          condition
        )
      )
  }.map(_.int(1)).single.apply().get

  /**
    * Counts the number of characters that an account has.
    *
    * @param accountId account id
    * @param session   database session
    * @return number of characters meeting condition
    */
  def countByAccount(accountId: Int)(implicit session: DBSession = autoSession): Int =
    count(Some(sqls.eq(c.accountId, accountId)))

  /**
    * Checks if character is owned by account
    *
    * @param accountId account
    * @param guid      character
    * @param session   database session
    * @return true if owned, false otherwise
    */
  def isOwner(accountId: Int, guid: Guid)(implicit session: DBSession = autoSession): Boolean =
    count(Some(sqls.eq(c.accountId, accountId).and.eq(c.guid, guid))) == 1
}


