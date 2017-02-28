package ensiwow.auth.crypto

import ensiwow.utils.BigIntExtensions._

/**
  * Created by sknz on 2/15/17.
  */
object Srp6Constants {
  // Constants shared with the client as part of the SRP6 protocol
  val g = BigInt(7)
  val N = BigInt("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16)

  // Byte arrays
  val NBytes: Array[Byte] = N.toUnsignedLBytes()
  val gBytes: Array[Byte] = g.toUnsignedLBytes()

  // Obtains a SHA-1 digest instance
  // Note that each call to getInstance returns a different instance
  private val messageDigest = java.security.MessageDigest.getInstance("SHA-1")

  // Hashes for g and N
  val NBytesDigest: Array[Byte] = messageDigest.digest(NBytes)
  val gBytesDigest: Array[Byte] = messageDigest.digest(gBytes)

  // XOR both digest to get t3
  val gDigestXorNDigest: Array[Byte] = NBytesDigest.zip(gBytesDigest).map { case (a, b) => a ^ b }.map(_.toByte)

  val legacyMultiplier = 3
}
