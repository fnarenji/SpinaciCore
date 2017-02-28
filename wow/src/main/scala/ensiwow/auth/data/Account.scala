package ensiwow.auth.data

import ensiwow.auth.crypto.{Srp6Identity, Srp6Protocol}

import scala.collection.mutable
import scalikejdbc._

/**
  * Created by sknz on 2/19/17.
  */
object Account {

  implicit val session = AutoSession

  private val srp6 = new Srp6Protocol()

  // TODO: remove this and replace with database
  private val sessionKeyByUser: mutable.HashMap[String, BigInt] = new mutable.HashMap[String, BigInt]

  def getSaltAndVerifier(userName: String): Srp6Identity = {
    // TODO: non hardcoded password
    val password = "t"

    // TODO: this should be computed a single time upon account creation
    srp6.computeSaltAndVerifier(userName, password)
  }

  def saveSessionKey(login: String, sessionKey: BigInt): Unit = {
    sessionKeyByUser.put(login, sessionKey)
      using(ConnectionPool.borrow()) { conn =>
        sql"update users set sessionkey=$sessionKey WHERE login=$login".update.apply()
      }
  }

  def getSessionKey(userName: String): BigInt = {
    sessionKeyByUser(userName)
  }

  def createAccount(login : String, password: String) = {

    val Srp6Identity(salt, verifier) = srp6.computeSaltAndVerifier(login, password)

    using(ConnectionPool.borrow()) { conn =>
      sql"insert into users (login,verifier,salt,sessionkey) VALUES ($login,$salt,$verifier,null)".update.apply()
    }

  }

}
