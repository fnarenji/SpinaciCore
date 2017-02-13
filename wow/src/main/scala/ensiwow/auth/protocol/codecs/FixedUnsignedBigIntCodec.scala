package ensiwow.auth.protocol.codecs

import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

/**
  * Encodes a unsigned big integer in a fixed number of bytes
  */
private[codecs] final class FixedUnsignedBigIntCodec(sizeInBytes: Long) extends Codec[BigInt] {
  require(sizeInBytes > 0, "size must be non null")

  /**
    * Size in bits
    */
  private val sizeInBits = sizeInBytes * 8L

  /**
    * Max value that can be stored on an unsigned big int of sizeInBits size
    */
  private val maxValue = BigInt(2).pow(sizeInBits.toInt)

  override def sizeBound: SizeBound = SizeBound.exact(sizeInBits)

  override def encode(value: BigInt): Attempt[BitVector] = {
    if (value < 0 || value >= maxValue) {
      return Attempt.failure(Err(s"Big number exceeds allowed range (0 <= bn < 2^$sizeInBits)"))
    }

    val byteArray = value.toByteArray
    val bits = BitVector(byteArray)

    val littleEndian = bits.reverseByteOrder

    // Remove leading zero bits that are used to indicate sign
    // Scala BigInts are signed but here we want to treat them as unsigned
    var unsignedBits = littleEndian
    while (unsignedBits.endsWith(BitVector.zero))
      unsignedBits = unsignedBits.dropRight(1)

    if (unsignedBits sizeGreaterThan sizeInBits) {
      Attempt.failure(Err(s"Big number exceeds max bits count of $sizeInBits (is ${littleEndian.size})"))
    }

    val resizedVector = unsignedBits.padRight(sizeInBits)

    Attempt.successful(resizedVector)
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[BigInt]] = {
     bits.acquire(sizeInBits) match {
      case Left(err) => Attempt.failure(Err(err))
      case Right(usableBits) =>
        // Add back a leading sign zero
        val signedBits = usableBits ++ BitVector.lowByte

        val bigEndianBytes = signedBits.reverseByteOrder

        val bigInt = BigInt(bigEndianBytes.toByteArray)

        Attempt.successful(DecodeResult[BigInt](bigInt, bits.drop(sizeInBits)))
    }
  }

  override def toString = s"BigIntCodec"
}
