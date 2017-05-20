package wow.client.auth

import java.security.MessageDigest

import wow.auth.protocol.packets.{ClientChallenge, ClientLogonProof, ServerLogonChallengeSuccess}
import wow.common.VersionInfo
import wow.utils.BigIntExtensions._


/**
  * This utility object implements the client side of the SRP6 authentication protocol
  */
object SRP6 {

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
    login = "T")

  val password = "t"

  val a = BigInt("0")

  var sharedKey: Array[Byte] = _

  /**
    * Computes a proof from the challenge sent by the server
    * @param challengeResponse the server's response to the challenge request
    * @return the proof
    */
  def computeProof(challengeResponse: ServerLogonChallengeSuccess): ClientLogonProof = {

    // Client's public key
    val A: BigInt = challengeResponse.g ^ a

    // Random scrambling parameter
    val u: BigInt = hash(A, challengeResponse.serverKey)

    // Client's private key
    val x: BigInt = hash(challengeResponse.salt, BigInt(password.getBytes))

    val sessionKey: BigInt = {
      val k = 3
      val session = (challengeResponse.serverKey - k * challengeResponse.g ^ x) ^ (a + u * x)
      hash(session)
    }

    sharedKey = computeSharedKey(sessionKey)

    // M = H(H(N) xor H(g), H(I), s, A, B, K)
    val clientProof: BigInt = {
      val Hn = hash(challengeResponse.N)
      val Hg = hash(BigInt(challengeResponse.g))
      hash(
        Hn ^ Hg,
        hash(BigInt(challengeRequest.login.getBytes)),
        challengeResponse.salt,
        A,
        challengeResponse.serverKey,
        sessionKey)
    }

    ClientLogonProof(A, clientProof, BigInt(0))
  }

  /**
    * Generates the shared key from the session key
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
    * Generates a SHA-1 hash from the arguments
    * @param bytes the data to be hashed
    * @return the hash
    */
  def hash(bytes: BigInt*): BigInt = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    for (b <- bytes) {
      messageDigest.update(b.toByteArray)
    }
    BigInt.fromUnsignedLBytes(messageDigest.digest)
  }
}
