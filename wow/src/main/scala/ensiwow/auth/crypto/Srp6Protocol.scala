package ensiwow.auth.crypto

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import ensiwow.auth.handlers.LogonChallengeHandler
import ensiwow.utils.BigIntExtensions._
import scodec.bits.ByteVector

import scala.util.Random



case class Srp6Identity(salt: BigInt, verifier: BigInt)

case class Srp6Challenge(smallB: BigInt, serverKey: BigInt)

case class Srp6Proof(serverProof: ByteVector, sharedKey: BigInt)

/**
  * SRP6 implementation
  */
class Srp6Protocol {

  import Srp6Protocol.FixedRandomMode

  // Obtains a SHA-1 digest instance
  // Note that each call to getInstance returns a different instance
  private val messageDigest = MessageDigest.getInstance("SHA-1")

  private val SaltSize = 32
  private val SaltSizeBits = SaltSize * 8
  private val SmallBBitCount = 19 * 8

  def computeSaltAndVerifier(userName: String, password: String): Srp6Identity = {
    // Auth string is made of uppercase password salted with upper case username
    val authString = s"${userName.toUpperCase}:${password.toUpperCase()}"
    val authBytes = authString.getBytes(StandardCharsets.US_ASCII)
    // Compute authBytes SHA digest
    val authDigest = messageDigest.digest(authBytes)

    val salt = if (FixedRandomMode) {
      LogonChallengeHandler.FixedSessionKey
    } else {
      BigInt(SaltSizeBits, Random)
    }
    assert(salt > 0)

    val saltBytes = salt.toNetworkBytes(SaltSize)

    // x is sha hash of s and auth digest
    messageDigest.update(saltBytes)
    messageDigest.update(authDigest)
    val saltedPasswordHash = messageDigest.digest
    val saltedPassword = BigInt.fromNetworkBytes(saltedPasswordHash)

    if (FixedRandomMode) {
      assert(saltedPassword == BigInt("550943625525122196566962046154797572093457682231"))
    }

    val verifier = Srp6Constants.g.modPow(saltedPassword, Srp6Constants.N)

    Srp6Identity(salt, verifier)
  }

  def computeChallenge(identity: Srp6Identity): Srp6Challenge = {
    val verifier = identity.verifier

    val smallB = if (FixedRandomMode) {
      // TODO: with some proper mocking this could be safely moved to unit tests
      LogonChallengeHandler.FixedSmallB
    } else {
      BigInt(SmallBBitCount, Random)
    }

    assert(smallB > 0)

    val gPowSmallB = Srp6Constants.g.modPow(smallB, Srp6Constants.N)
    val serverKey = ((verifier * Srp6Constants.legacyMultiplier) + gPowSmallB) % Srp6Constants.N

    // TODO: with some proper mocking this could be safely moved to unit tests
    if (FixedRandomMode) {
      assert(verifier == BigInt("22075422366936545515674385768650057420632007409200282980604073279761866516199"))
      assert(gPowSmallB == BigInt("29939248540161975887379129838367961798422604125188925685493670515789973246671"))
      assert(serverKey == BigInt("34065449131815595092332791003415184197068868016788977698739449184781844147149"))
    }

    Srp6Challenge(smallB, serverKey)
  }

  def verify(login: String,
             clientKey: BigInt,
             clientProof: BigInt,
             identity: Srp6Identity,
             challenge: Srp6Challenge): Option[Srp6Proof] = {
    val clientKeyBytes = clientKey.toNetworkBytes()
    val serverKeyBytes = challenge.serverKey.toNetworkBytes()

    val sessionKey = computeSessionKey(challenge.smallB, identity.verifier, clientKey, clientKeyBytes, serverKeyBytes)

    val sharedKeyBytes = computeSharedKey(sessionKey)
    val sharedKey = BigInt.fromNetworkBytes(sharedKeyBytes)

    val loginDigest = messageDigest.digest(login.toUpperCase().getBytes(StandardCharsets.US_ASCII))

    val expectedProofBytes = computeExpectedProof(identity.salt,
      clientKeyBytes,
      serverKeyBytes,
      sharedKeyBytes,
      loginDigest)

    val clientProofBytes = clientProof.toNetworkBytes()

    if (clientProofBytes sameElements expectedProofBytes) {
      val serverProof = computeServerProof(clientKeyBytes, sharedKeyBytes, clientProofBytes)
      val serverProofAsByteVector = ByteVector.view(serverProof)

      Some(Srp6Proof(serverProofAsByteVector, sharedKey))
    } else {
      None
    }
  }

  private def computeServerProof(clientKeyBytes: Array[Byte],
                                 sharedKeyBytes: Array[Byte],
                                 clientProofBytes: Array[Byte]): Array[Byte] = {
    messageDigest.update(clientKeyBytes)
    messageDigest.update(clientProofBytes)
    messageDigest.update(sharedKeyBytes)

    messageDigest.digest()
  }

  private def computeExpectedProof(salt: BigInt,
                                   clientKeyBytes: Array[Byte],
                                   serverKeyBytes: Array[Byte],
                                   sharedKeyBytes: Array[Byte],
                                   loginDigest: Array[Byte]): Array[Byte] = {
    messageDigest.update(Srp6Constants.gDigestXorNDigest)
    messageDigest.update(loginDigest)
    messageDigest.update(salt.toNetworkBytes())
    messageDigest.update(clientKeyBytes)
    messageDigest.update(serverKeyBytes)
    messageDigest.update(sharedKeyBytes)

    messageDigest.digest()
  }

  private def computeSharedKey(sessionKey: BigInt) = {
    val sessionKeyBytes = sessionKey.toNetworkBytes()
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

  private def computeSessionKey(smallB: BigInt,
                                verifier: BigInt,
                                clientKey: BigInt,
                                clientKeyBytes: Array[Byte],
                                serverKeyBytes: Array[Byte]): BigInt = {
    messageDigest.update(clientKeyBytes)
    messageDigest.update(serverKeyBytes)
    val shaDigest = messageDigest.digest
    val u = BigInt.fromNetworkBytes(shaDigest)

    (clientKey * verifier.modPow(u, Srp6Constants.N)).modPow(smallB, Srp6Constants.N)
  }

}

object Srp6Protocol {
  val FixedRandomMode = true
}
