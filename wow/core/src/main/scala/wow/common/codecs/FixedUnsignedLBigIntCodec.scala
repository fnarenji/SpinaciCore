package wow.common.codecs

import wow.utils.BigIntExtensions._
import scodec.bits.{BitVector, ByteVector}
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

/**
  * Encodes a unsigned big integer in a fixed number of bytes
  */
private[codecs] final class FixedUnsignedLBigIntCodec(sizeInBytes: Long) extends Codec[BigInt] {
  require(sizeInBytes > 0, "size must be non null")
  assert(sizeInBytes.toInt == sizeInBytes)

  /**
    * Size in bits
    */
  private val sizeInBits = sizeInBytes * 8L

  override def sizeBound: SizeBound = SizeBound.exact(sizeInBits)

  override def encode(value: BigInt): Attempt[BitVector] = {
    try {
      val valueBytes = value.toUnsignedLBytes(sizeInBytes.toInt)

      val valueBits = ByteVector.view(valueBytes).bits

      Attempt.successful(valueBits)
    } catch {
      case e: IllegalArgumentException => Attempt.failure(Err(e.toString))
    }
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[BigInt]] = {
    bits.acquire(sizeInBits) match {
      case Left(err) => Attempt.failure(Err(err))
      case Right(usableBits) =>
        val bigInt = BigInt.fromUnsignedLBytes(usableBits.toByteArray)

        Attempt.successful(DecodeResult(bigInt, bits.drop(sizeInBits)))
    }
  }

  override def toString = s"BigIntCodec"
}
