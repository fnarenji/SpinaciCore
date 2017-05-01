package ensiwow.auth.data


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ensiwow.auth.crypto.{Srp6Identity, Srp6Protocol}
import ensiwow.api.API
import ensiwow.common.database.Databases
import ensiwow.utils.BigIntExtensions._
import scalikejdbc._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

final case class Account(
  id: Long,
  login: String,
  var identity: Srp6Identity,
  var sessionKey: Option[BigInt] = None)

/**
  * Account information
  */
object Account extends SQLSyntaxSupport[Account] {
  override def connectionPoolName: Any = Databases.AuthServer

  def apply(s: SyntaxProvider[Account])(rs: WrappedResultSet): Account = apply(s.resultName)(rs)

  def apply(rn: ResultName[Account])(rs: WrappedResultSet): Account = {
    val id = rs.long(column.id)
    val login = rs.string(column.login)

    val salt = rs.bigInt(column.c("salt"))
    val verifier = rs.bigInt(column.c("verifier"))
    val identity = Srp6Identity(salt, verifier)

    val sessionKey = rs.bigIntOpt(column.sessionKey)

    Account(id, login, identity, sessionKey)
  }

  def find(condition: SQLSyntax)(implicit session: DBSession = autoSession): Option[Account] = withSQL {
    select(column.*)
      .from(Account as syntax)
      .where
      .append(condition)
  }.map(Account(syntax)).single.apply()

  def findByLogin(login: String)(implicit session: DBSession = autoSession): Option[Account] =
    find(sqls.eq(column.login, login))

  def findById(id: Long)(implicit session: DBSession = autoSession): Option[Account] =
    find(sqls.eq(column.id, id))

  def save(account: Account)(implicit session: DBSession = autoSession): Int = withSQL {
    update(Account)
      .set(
        column.c("salt") -> account.identity.salt,
        column.c("verifier") -> account.identity.verifier,
        column.sessionKey -> account.sessionKey
      )
      .where
      .eq(column.id, account.id)
  }.update.apply()

  def saveIdentity(login: String, identity: Srp6Identity)(implicit session: DBSession = autoSession): Unit = assert(
    withSQL {
      update(Account)
        .set(
          column.c("salt") -> identity.salt,
          column.c("verifier") -> identity.verifier
        )
        .where
        .eq(column.login, login)
    }.update.apply() > 0)

  def saveSessionKey(login: String, sessionKey: BigInt)(implicit session: DBSession = autoSession): Unit = assert(
    withSQL {
      update(Account)
        .set(
          column.sessionKey -> sessionKey
        )
        .where
        .eq(column.login, login)
    }.update.apply() > 0)

  def create(login: String, identity: Srp6Identity)(implicit session: DBSession = autoSession): Long = withSQL {
    insert.into(Account)
      .namedValues(
        column.login -> login,
        column.c("salt") -> identity.salt,
        column.c("verifier") -> identity.verifier
      )
  }.updateAndReturnGeneratedKey.apply()
}

object AccountAPI extends API {

  case class AccountReq(login: String, password: String)

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  implicit val userFormat: RootJsonFormat[AccountReq] = jsonFormat2(AccountReq.apply)

  override val route: Route =
    pathPrefix("account") {
      get {
        path("online") {
          pathEnd {
            // TODO: Evalute online users
            complete(StatusCodes.OK)
          }
        }
      } ~
        post {
          path("create") {
            pathEnd {
              entity(as[AccountReq]) { accountReq =>
                // TODO: Check if already existing account
                // It must not exist
                val srp = new Srp6Protocol()
                val identity = srp.computeSaltAndVerifier(accountReq.login, accountReq.password)

                Account.create(accountReq.login, identity)
                complete(StatusCodes.Created)
              }
            }
          } ~
            path("delete") {
              pathEnd {
                entity(as[String]) { login =>
                  // TODO: Delete account
                  // It must exist
                  // Account.deleteAccount(user.username, user.password)
                  complete(StatusCodes.NotImplemented)
                }
              }
            }
        } ~
        put {
          path("reinitialize") {
            pathEnd {
              entity(as[AccountReq]) { accountReq =>
                Account.findByLogin(accountReq.login) match {
                  case Some(account: Account) =>
                    val srp = new Srp6Protocol()
                    val identity: Srp6Identity = srp.computeSaltAndVerifier(accountReq.login, accountReq.password)
                    account.identity = identity

                    Account.save(account)
                    complete(StatusCodes.OK)
                  case None =>
                    complete(StatusCodes.BadRequest)
                }
              }
            }
          }
        }
    }
}


