package wow.client.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import wow.auth.crypto.{DefaultRandomBigInt, RandomBigInt, Srp6Constants}
import wow.auth.protocol.packets.{ClientChallenge, ClientLogonProof, ServerLogonChallengeSuccess}
import wow.common.VersionInfo
import wow.utils.BigIntExtensions._


/**
  * This utility object implements the client side of the SRP6 authentication protocol
  */
object Srp6Client {

  val login = "t"
  val password = "t"

  /**
    * A standard logon challenge request
    */
  val challengeRequest = ClientChallenge(
    error = 8,
    size = 31,
    VersionInfo.SupportedVersionInfo,
    platform = "x86",
    os = "OSX",
    country = "enUS",
    timezoneBias = 120,
    ip = Vector(127, 0, 0, 1),
    login = login.toUpperCase)


  private val ClientPrivateKeyBitCount = 19 * 8
  private val randomBigInt: RandomBigInt = new DefaultRandomBigInt

  /**
    * Computes a proof from the challenge sent by the server
    *
    * @param challenge the server's response to the challenge request
    * @return the proof packet
    */
  def computeProof(challenge: ServerLogonChallengeSuccess): ClientLogonProof = {

    val clientPrivateKey: BigInt = randomBigInt.next(ClientPrivateKeyBitCount)
    val clientKey: BigInt = Srp6Constants.g.modPow(clientPrivateKey, Srp6Constants.N)

    val loginDigest: Array[Byte] = {
      val messageDigest = MessageDigest.getInstance("SHA-1")
      messageDigest.digest(login.toUpperCase().getBytes(StandardCharsets.US_ASCII))
    }

    val passwordHash: BigInt = {
      val messageDigest = MessageDigest.getInstance("SHA-1")
      messageDigest.update(challenge.salt.toUnsignedLBytes())
      messageDigest.update(password.getBytes(StandardCharsets.US_ASCII))
      BigInt.fromUnsignedLBytes(messageDigest.digest())
    }

    val sessionKey: BigInt = computeSessionKey(passwordHash, clientPrivateKey, clientKey, challenge.serverKey)
    println(s"Client - Session key: $sessionKey")

    val sharedKey: Array[Byte] = computeSharedKey(sessionKey)
    println(s"Client - Shared key: $sharedKey")

    val clientProof: Array[Byte] = computeProof(
      challenge.salt.toUnsignedLBytes(),
      clientKey.toUnsignedLBytes(),
      challenge.serverKey.toUnsignedLBytes(),
      sharedKey,
      loginDigest
    )

    ClientLogonProof(clientKey, BigInt.fromUnsignedLBytes(clientProof), BigInt(0))
  }

  /**
    * Computes the challenge's proof
    * @param saltBytes the salt
    * @param clientKeyBytes client's key
    * @param serverKeyBytes server's key
    * @param sharedKeyBytes shared key
    * @param loginDigest login's hash
    * @return the proof
    */
  private def computeProof(saltBytes: Array[Byte],
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
    * @param passwordHash     a hash of the salt with the password
    * @param clientPrivateKey client's private key
    * @param clientKey        client's key
    * @param serverKey        server's key
    * @return a session key
    */
  private def computeSessionKey(passwordHash: BigInt,
                                clientPrivateKey: BigInt,
                                clientKey: BigInt,
                                serverKey: BigInt): BigInt = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    messageDigest.update(clientKey.toUnsignedLBytes())
    messageDigest.update(serverKey.toUnsignedLBytes())
    val shaDigest = messageDigest.digest
    val u = BigInt.fromUnsignedLBytes(shaDigest)

    (serverKey - Srp6Constants.legacyMultiplier * Srp6Constants.g.modPow(passwordHash, Srp6Constants.N))
      .modPow(clientPrivateKey + u * passwordHash, Srp6Constants.N)
  }

}
