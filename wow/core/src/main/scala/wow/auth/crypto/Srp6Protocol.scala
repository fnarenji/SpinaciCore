package wow.auth.crypto

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import wow.utils.BigIntExtensions._
import scodec.bits.ByteVector

import scala.util.Random

/**
  * SRP6 identity information
  *
  * @param salt     salt used for verifier
  * @param verifier verifier against which to compare
  */
case class Srp6Identity(salt: BigInt, verifier: BigInt)

/**
  * SRP6 challenge information.
  *
  * @param serverPrivateKey random value generated
  * @param serverKey        server key used by client
  */
case class Srp6Challenge(serverPrivateKey: BigInt, serverKey: BigInt)

/**
  * SRP6 validated proof information.
  *
  * @param serverProof server proof as byte vector
  * @param sharedKey   shared key
  */
case class Srp6Proof(serverProof: ByteVector, sharedKey: BigInt)

trait RandomBigInt {
  def next(sizeInBits: Int): BigInt
}

class DefaultRandomBigInt(random: Random = Random) extends RandomBigInt {
  def next(sizeInBits: Int) = BigInt(sizeInBits, random)
}

/**
  * SRP6 protocol implementation.
  * This class keeps no state about ongoing SRP6 computation (i.e. all state is stored in SRP6*Data objects)
  * Therefore, it is safe to reuse this class to compute overlapping SRP6 calculations.
  *
  * The only state maintained by this state is its MessageDigest instance used for computing hashes.
  *
  * @note Due to the presence of the MessageDigest instance, this class is NOT thread safe.
  */
class Srp6Protocol(val randomBigInt: RandomBigInt = new DefaultRandomBigInt) {
  // Obtains a SHA-1 digest instance
  // Note that each call to getInstance returns a different instance
  private val messageDigest = MessageDigest.getInstance("SHA-1")

  /**
    * Size of randomly generated salt
    */
  private val SaltSize = 32

  /**
    * Size of randomly generated salt in bits
    */
  private val SaltSizeBits = SaltSize * 8

  /**
    * Size of random number 'b' in bits
    */
  private val ServerPrivateKeyBitCount = 19 * 8

  /**
    * Computes salt and verifier for user credentials
    * This should be used once upon account creation (e.g. from a command line interface).
    * Passwords should never travel the network in clear.
    *
    * @param login    user name
    * @param password password
    * @return Srp6Identity representing user information
    */
  def computeSaltAndVerifier(login: String, password: String): Srp6Identity = {
    // Auth string is made of uppercase password salted with upper case username
    val authString = s"${login.toUpperCase}:${password.toUpperCase()}"
    val authBytes = authString.getBytes(StandardCharsets.US_ASCII)
    // Compute authBytes SHA digest
    val authDigest = messageDigest.digest(authBytes)

    val salt = randomBigInt.next(SaltSizeBits)
    assert(salt > 0)

    val saltBytes = salt.toUnsignedLBytes(SaltSize)

    // x is sha hash of s and auth digest
    messageDigest.update(saltBytes)
    messageDigest.update(authDigest)
    val saltedPasswordHash = messageDigest.digest
    val saltedPassword = BigInt.fromUnsignedLBytes(saltedPasswordHash)

    val verifier = Srp6Constants.g.modPow(saltedPassword, Srp6Constants.N)

    Srp6Identity(salt, verifier)
  }

  /**
    * Computes a server side challenge for this identity to be sent to the client
    *
    * @param identity identity for which the challenge will be computed
    * @return a challenge which must be met by the client
    */
  def computeChallenge(identity: Srp6Identity): Srp6Challenge = {
    val verifier = identity.verifier

    val serverPrivateKey = randomBigInt.next(ServerPrivateKeyBitCount)
    assert(serverPrivateKey > 0)

    val gPowServerPrivateKey = Srp6Constants.g.modPow(serverPrivateKey, Srp6Constants.N)
    val serverKey = ((verifier * Srp6Constants.legacyMultiplier) + gPowServerPrivateKey) % Srp6Constants.N

    Srp6Challenge(serverPrivateKey, serverKey)
  }

  /**
    * Validates a proof sent by the client in response to a challenge.
    *
    * @param login       user name
    * @param clientKey   ephemeral key sent by the client
    * @param clientProof proof sent by the client
    * @param identity    identity this proof must be verified against
    * @param challenge   challenge which this proofs answers
    * @return None if the verification failed, an Srp6Proof object containing the results of the exchange otherwise
    */
  def verify(login: String,
             clientKey: BigInt,
             clientProof: BigInt,
             identity: Srp6Identity,
             challenge: Srp6Challenge): Option[Srp6Proof] = {
    val clientKeyBytes = clientKey.toUnsignedLBytes()
    val serverKeyBytes = challenge.serverKey.toUnsignedLBytes()

    val sessionKey = computeSessionKey(challenge.serverPrivateKey,
      identity.verifier,
      clientKey,
      clientKeyBytes,
      serverKeyBytes)

    val sharedKeyBytes = computeSharedKey(sessionKey)
    val sharedKey = BigInt.fromUnsignedLBytes(sharedKeyBytes)

    val loginDigest = messageDigest.digest(login.toUpperCase().getBytes(StandardCharsets.US_ASCII))

    val expectedProofBytes = computeExpectedProof(identity.salt,
      clientKeyBytes,
      serverKeyBytes,
      sharedKeyBytes,
      loginDigest)

    val clientProofBytes = clientProof.toUnsignedLBytes()

    if (clientProofBytes sameElements expectedProofBytes) {
      val serverProof = computeServerProof(clientKeyBytes, sharedKeyBytes, clientProofBytes)
      val serverProofAsByteVector = ByteVector.view(serverProof)

      Some(Srp6Proof(serverProofAsByteVector, sharedKey))
    } else {
      None
    }
  }

  def reverify(login: String, random: BigInt, clientKey: BigInt, clientProof: BigInt, sessionKey: BigInt): Boolean = {
    val clientKeyBytes = clientKey.toUnsignedLBytes()

    val loginDigest = messageDigest.digest(login.toUpperCase().getBytes(StandardCharsets.US_ASCII))

    val randomBytes = random.toUnsignedLBytes()

    val sessionKeyBytes = sessionKey.toUnsignedLBytes()

    messageDigest.update(clientKeyBytes)
    messageDigest.update(loginDigest)
    messageDigest.update(randomBytes)
    messageDigest.update(sessionKeyBytes)

    val expectedProofBytes = messageDigest.digest()

    val clientProofBytes = clientProof.toUnsignedLBytes()

    clientProofBytes sameElements expectedProofBytes
  }

  /**
    * Computes server proof. H(clientKey, sharedKey, clientProf)
    *
    * @param clientKeyBytes   client key as byte array
    * @param sharedKeyBytes   shared key as byte array
    * @param clientProofBytes client proof as byte array
    * @return server proof as a byte array
    */
  private def computeServerProof(
                                  clientKeyBytes: Array[Byte],
                                  sharedKeyBytes: Array[Byte],
                                  clientProofBytes: Array[Byte]): Array[Byte] = {
    messageDigest.update(clientKeyBytes)
    messageDigest.update(clientProofBytes)
    messageDigest.update(sharedKeyBytes)

    messageDigest.digest()
  }

  /**
    * Computes the proof the client is expected to send.
    * H(H(g) Xor H(N), H(upper(login)), salt, clientKey, serverKey, sharedKey)
    *
    * @param salt           salt used for computation
    * @param clientKeyBytes client key as byte array
    * @param serverKeyBytes server key as byte array
    * @param sharedKeyBytes shared key as byte array
    * @param loginDigest    hash of uppercase login
    * @return expect proof as byte array
    */
  private def computeExpectedProof(
                                    salt: BigInt,
                                    clientKeyBytes: Array[Byte],
                                    serverKeyBytes: Array[Byte],
                                    sharedKeyBytes: Array[Byte],
                                    loginDigest: Array[Byte]): Array[Byte] = {
    messageDigest.update(Srp6Constants.gDigestXorNDigest)
    messageDigest.update(loginDigest)
    messageDigest.update(salt.toUnsignedLBytes())
    messageDigest.update(clientKeyBytes)
    messageDigest.update(serverKeyBytes)
    messageDigest.update(sharedKeyBytes)

    messageDigest.digest()
  }

  /**
    * Computes the shared key.
    * Interleave(H(sessionKey.evenBytes), H(sessionKey.oddBytes))
    *
    * @param sessionKey session key from which shared key is derived
    * @return shared key
    */
  private def computeSharedKey(sessionKey: BigInt) = {
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
    * Computes the session key.
    * u = H(clientKey, serverKey)
    * ((clientKey * (verifier &#94; u) % N) &#94; serverPrivateKey) % N
    *
    * @param serverPrivateKey random constant computed during challenge
    * @param verifier         verifier for identity being validated
    * @param clientKey        client key
    * @param clientKeyBytes   client key as byte array
    * @param serverKeyBytes   server key as byte array
    * @return session key
    */
  private def computeSessionKey(
                                 serverPrivateKey: BigInt,
                                 verifier: BigInt,
                                 clientKey: BigInt,
                                 clientKeyBytes: Array[Byte],
                                 serverKeyBytes: Array[Byte]): BigInt = {
    messageDigest.update(clientKeyBytes)
    messageDigest.update(serverKeyBytes)
    val shaDigest = messageDigest.digest
    val u = BigInt.fromUnsignedLBytes(shaDigest)

    (clientKey * verifier.modPow(u, Srp6Constants.N)).modPow(serverPrivateKey, Srp6Constants.N)
  }

}
