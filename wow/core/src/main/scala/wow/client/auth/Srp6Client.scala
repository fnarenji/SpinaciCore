package wow.client.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import wow.auth.crypto.{DefaultRandomBigInt, RandomBigInt, Srp6Constants}
import wow.auth.protocol.packets.{ClientLogonProof, ServerLogonChallengeSuccess}
import AccountEntry
import wow.utils.BigIntExtensions._

case class AuthClientConfig(ip: Vector[Int], login: String, password: String) {
  login.toUpperCase
}

/**
  * This utility object implements the client side of the SRP6 authentication protocol
  */
object Srp6Client {

  /**
    * A standard logon challenge request
    */


  private val ClientPrivateKeyBitCount = 19 * 8
  private val randomBigInt: RandomBigInt = new DefaultRandomBigInt

  /**
    * Computes a proof from the challenge sent by the server
    *
    * @param challenge the server's response to the challenge request
    * @return the proof packet
    */
  def computeProof(account: AccountEntry, challenge: ServerLogonChallengeSuccess): ClientLogonProof = {

    val messageDigest = MessageDigest.getInstance("SHA-1")

    val authString = s"${account.login.toUpperCase}:${account.password.toUpperCase}"
    val authBytes = authString.getBytes(StandardCharsets.US_ASCII)
    val authDigest = messageDigest.digest(authBytes)

    messageDigest.update(challenge.salt.toUnsignedLBytes())
    messageDigest.update(authDigest)
    val saltedPasswordHash = messageDigest.digest()
    val saltedPassword = BigInt.fromUnsignedLBytes(saltedPasswordHash)

    val clientPrivateKey = randomBigInt.next(ClientPrivateKeyBitCount)
    val clientKey = Srp6Constants.g.modPow(clientPrivateKey, Srp6Constants.N)

    val sessionKey = computeSessionKey(saltedPassword,
      clientPrivateKey,
      clientKey,
      challenge.serverKey)
    println(s"Client - Session key: $sessionKey")

    val sharedKeyBytes: Array[Byte] = computeSharedKey(sessionKey)
    val sharedKey = BigInt.fromUnsignedLBytes(sharedKeyBytes)
    println(s"Client - Shared key: $sharedKey")

    val clientProof: Array[Byte] = computeProof(
      challenge.salt.toUnsignedLBytes(),
      clientKey.toUnsignedLBytes(),
      challenge.serverKey.toUnsignedLBytes(),
      sharedKeyBytes,
      messageDigest.digest(account.login.toUpperCase().getBytes(StandardCharsets.US_ASCII))
    )

    ClientLogonProof(clientKey, BigInt.fromUnsignedLBytes(clientProof), BigInt(0))
  }

  /**
    * Computes the challenge's proof
    *
    * @param saltBytes      the salt
    * @param clientKeyBytes client's key
    * @param serverKeyBytes server's key
    * @param sharedKeyBytes shared key
    * @param loginDigest    login's hash
    * @return the proof
    */
  private def computeProof(
    saltBytes: Array[Byte],
    clientKeyBytes: Array[Byte],
    serverKeyBytes: Array[Byte],
    sharedKeyBytes: Array[Byte],
    loginDigest: Array[Byte]): Array[Byte] = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    messageDigest.update(Srp6Constants.gDigestXorNDigest)
    messageDigest.update(loginDigest)
    messageDigest.update(saltBytes)
    messageDigest.update(clientKeyBytes)
    messageDigest.update(serverKeyBytes)
    messageDigest.update(sharedKeyBytes)

    messageDigest.digest()
  }

  /**
    * Generates the shared key from the session key
    *
    * @param sessionKey the session key
    * @return the shared key
    */
  private def computeSharedKey(sessionKey: BigInt): Array[Byte] = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    val sessionKeyBytes = sessionKey.toUnsignedLBytes()
    val evenBytes = sessionKeyBytes.sliding(1, 2).flatten.toArray
    val oddBytes = sessionKeyBytes.drop(1).sliding(1, 2).flatten.toArray

    val evenDigest = messageDigest.digest(evenBytes)
    val oddDigest = messageDigest.digest(oddBytes)

    val sharedKeyBytes = new Array[Byte](40)

    for (i <- 0 until messageDigest.getDigestLength) {
      sharedKeyBytes(2 * i) = evenDigest(i)
      sharedKeyBytes(2 * i + 1) = oddDigest(i)
    }

    sharedKeyBytes
  }

  /**
    * Computes the session key
    *
    * @param authDigest       a hash of the salt with the password
    * @param clientPrivateKey client's private key
    * @param clientKey        client's key
    * @param serverKey        server's key
    * @return a session key
    */
  private def computeSessionKey(
    authDigest: BigInt,
    clientPrivateKey: BigInt,
    clientKey: BigInt,
    serverKey: BigInt): BigInt = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    messageDigest.update(clientKey.toUnsignedLBytes())
    messageDigest.update(serverKey.toUnsignedLBytes())
    val shaDigest = messageDigest.digest
    val u = BigInt.fromUnsignedLBytes(shaDigest)

    (serverKey - Srp6Constants.legacyMultiplier * Srp6Constants.g.modPow(authDigest, Srp6Constants.N))
      .modPow(clientPrivateKey + u * authDigest, Srp6Constants.N)
  }

}
