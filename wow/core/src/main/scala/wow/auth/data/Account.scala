package wow.auth.data

import scalikejdbc._
import wow.auth.crypto.Srp6Identity
import wow.common.database.{Databases, RichColumn, databasecomponent}
import wow.utils.BigIntExtensions._

/**
  * Account information
  */
final case class Account(
  id: Int,
  login: String,
  @databasecomponent
  var identity: Srp6Identity,
  var sessionKey: Option[BigInt] = None)

object Account extends SQLSyntaxSupport[Account] with RichColumn[Account] {
  override def connectionPoolName: Any = Databases.AuthServer

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = apply(s.resultName)(rs)

  def apply(rn: ResultName[Account])(rs: WrappedResultSet): Account = {
    val id = rs.int(c.id)
    val login = rs.string(c.login)

    val salt = rs.bigInt(c.salt)
    val verifier = rs.bigInt(c.verifier)
    val identity = Srp6Identity(salt, verifier)

    val sessionKey = rs.bigIntOpt(c.sessionKey)

    Account(id, login, identity, sessionKey)
  }

  def create(login: String, identity: Srp6Identity)(implicit session: DBSession = autoSession): Int = withSQL {
    insert.into(Account)
      .namedValues(
        c.login -> login.toUpperCase(),
        c.salt -> identity.salt,
        c.verifier -> identity.verifier
      )
  }.updateAndReturnGeneratedKey.apply().toInt

  def save(account: Account)(implicit session: DBSession = autoSession): Int = withSQL {
    update(Account)
      .set(
        c.salt -> account.identity.salt,
        c.verifier -> account.identity.verifier,
        c.sessionKey -> account.sessionKey
      )
      .where
      .eq(c.id, account.id)
  }.update.apply()

  def find(condition: SQLSyntax)(implicit session: DBSession = autoSession): Option[Account] = withSQL {
    select(column.*)
      .from(Account as syntax)
      .where
      .append(condition)
  }.map(Account(syntax)).single.apply()

  def findByLogin(login: String)(implicit session: DBSession = autoSession): Option[Account] =
    find(sqls.eq(c.login, login))

  def findById(id: Int)(implicit session: DBSession = autoSession): Option[Account] =
    find(sqls.eq(c.id, id))
}

