package wow.realm.crypto

import wow.utils.BigIntExtensions._
import org.scalatest.{FlatSpec, Matchers}
import scodec.bits.{ByteVector, _}

/**
  * Created by sknz on 3/23/17.
  */
class SessionCipherTest extends FlatSpec with Matchers {
  behavior of "SessionCipher"
  private val sessionKey = BigInt.fromUnsignedLBytes(
    hex"0x4EE9401C869593B43EDEA515C59FDBF0C3E7C9BC7E4912815CE177B7FA6CA2D735B8C8651952B2A6".reverse.toArray
  )

  val cipher = new SessionCipher(sessionKey)

  it should "decipher value as expected" in {
    def decrypt(crypted: ByteVector, decrypted: ByteVector) = {
      val cryptedArray = crypted.toArray
      cipher.decrypt(cryptedArray)
      cryptedArray shouldEqual decrypted.toArray
    }

    decrypt(hex"2FB030BAE9F7", hex"0004ff040000")
    decrypt(hex"9DD64EA5DD8B", hex"000437000000")
    decrypt(hex"A841E178BDD6", hex"00088C030000")
    decrypt(hex"868813402A0E", hex"000CDC010000")
  }
}
