package ensiwow.auth.data

import ensiwow.auth.crypto.{Srp6Identity, Srp6Protocol}

import scala.collection.mutable

/**
  * Created by sknz on 2/19/17.
  */
object Account {
  private val srp6 = new Srp6Protocol()

  // TODO: remove this and replace with database
  private val sessionKeyByUser: mutable.HashMap[String, BigInt] = new mutable.HashMap[String, BigInt]

  def getSaltAndVerifier(userName: String): Srp6Identity = {
    // TODO: non hardcoded password
    val password = "t"

    // TODO: this should be computed a single time upon account creation
    srp6.computeSaltAndVerifier(userName, password)
  }

  def saveSessionKey(userName: String, sessionKey: BigInt): Unit = {
    sessionKeyByUser.put(userName, sessionKey)
  }

  def getSessionKey(userName: String): Option[BigInt] = {
    sessionKeyByUser.get(userName)
  }
}
