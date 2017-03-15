package ensiwow.auth.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ensiwow.api.API
import ensiwow.auth.crypto.{Srp6Identity, Srp6Protocol}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.collection.mutable

final case class Account(login: Account.Login, password: String)

/**
  * Account information
  */
object Account {
  type Login = String

  private val srp6 = new Srp6Protocol()

  // TODO: remove this and replace with database
  private val sessionKeyByUser: mutable.HashMap[String, BigInt] = new mutable.HashMap[String, BigInt]

  def getSaltAndVerifier(login: Login): Srp6Identity = {
    // TODO: non hardcoded password
    val password = "t"

    // TODO: this should be computed a single time upon account creation
    srp6.computeSaltAndVerifier(login, password)
  }

  def saveSessionKey(login: Login, sessionKey: BigInt): Unit = {
    sessionKeyByUser.put(login, sessionKey)
  }

  def getSessionKey(login: Login): Option[BigInt] = {
    sessionKeyByUser.get(login)
  }

  def createAccount(login: Login, password: String) = {

  }
}

object AccountAPI extends API {
  implicit val userFormat: RootJsonFormat[Account] = jsonFormat2(Account.apply)

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
              entity(as[Account]) { account =>
                // TODO: Check if already existing account
                // It must not exist
                Account.createAccount(account.login, account.password)
                complete(StatusCodes.Created)
              }
            }
          } ~
            path("delete") {
              pathEnd {
                entity(as[Account.Login]) { login =>
                  // TODO: Delete account
                  // It must exist
                  // Account.deleteAccount(user.username, user.password)
                  complete(StatusCodes.OK)
                }
              }
            }
        } ~
        put {
          path("reinitialize") {
            pathEnd {
              entity(as[Account]) { user =>
                // It must exist
                // TODO: Reinitialize account's password
                // Account.createAccount(user.username, user.password)
                complete(StatusCodes.OK)
              }
            }
          }
        }
    }
}

