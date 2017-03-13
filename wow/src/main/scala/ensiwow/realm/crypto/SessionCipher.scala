package ensiwow.realm.crypto

import ensiwow.utils.BigIntExtensions._
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.engines.RC4Engine
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter

/**
  * Two-way arc4-drop1024 cipher used to exchange packet with clients.
  * Only packet headers should be encrypted.
  */
class SessionCipher(sessionKey: BigInt) {
  val DropBytesCount = 1024

  private val encryptionKey =
    Array(0xCC, 0x98, 0xAE, 0x04, 0xE8, 0x97, 0xEA, 0xCA, 0x12, 0xDD, 0xC0, 0x93, 0x42, 0x91, 0x53, 0x57)
      .map(_.toByte)

  private val decryptionKey =
    Array(0xC2, 0xB3, 0x72, 0x3C, 0xC6, 0xAE, 0xD9, 0xB5, 0x34, 0x3C, 0x53, 0xEE, 0x2F, 0x43, 0x67, 0xCE)
      .map(_.toByte)

  private val encryptionEngine = initEngine(encrypt = true, encryptionKey)
  private val decryptionEngine = initEngine(encrypt = false, decryptionKey)

  /**
    * In place encryption of whole array
    * @param bytes array to encrypt
    * @return number of bytes encrypted
    */
  def encrypt(bytes: Array[Byte]): Int = {
    encryptionEngine.processBytes(bytes, 0, bytes.length, bytes, 0)
  }

  /**
    * In place decryption of whole array
    * @param bytes array to decrypt
    * @return number of bytes decrypted
    */
  def decrypt(bytes: Array[Byte]): Int = {
    decryptionEngine.processBytes(bytes, 0, bytes.length, bytes, 0)
  }

  /**
    * Initializes an RC4 crypto engine
    * @param encrypt true if encrypting, false if decrypting
    * @param key mutual key
    * @return initialized rc4 engine
    */
  private def initEngine(encrypt: Boolean, key: Array[Byte]) = {
    // Initializes HMac engine with key as parameter
    val hmac = new HMac(new SHA1Digest())
    hmac.init(new KeyParameter(key))

    // Hash session key using hmac with key
    val sessionKeyBytes = sessionKey.toUnsignedLBytes()
    hmac.update(sessionKeyBytes, 0, sessionKeyBytes.length)

    // Extract result in array
    val hash = new Array[Byte](hmac.getMacSize)
    hmac.doFinal(hash, 0)

    // Initialize cryptographic engine with hashed key
    val engine = new RC4Engine
    engine.init(encrypt, new KeyParameter(hash))

    // drop the first 1024 bytes to avoid leaking cryptographic information
    val dropBytes = Array.fill[Byte](DropBytesCount)(0)
    engine.processBytes(dropBytes, 0, dropBytes.length, dropBytes, 0)

    engine
  }

}
