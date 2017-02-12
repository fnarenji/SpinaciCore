package ensiwow.auth.protocol.codecs

import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, SizeBound}

/**
  * Created by sknz on 2/7/17.
  */
private[codecs] final class FixedUnsignedBigIntCodec(sizeInBytes: Long) extends Codec[BigInt] {
  val sizeInBits = sizeInBytes * 8L

  val maxValue = BigInt(2).pow(sizeInBits.toInt)

  override def sizeBound: SizeBound = SizeBound.exact(sizeInBits)

  override def encode(value: BigInt): Attempt[BitVector] = {
    if (value < 0 || value >= maxValue) {
      throw new IllegalArgumentException(s"Big number exceeds allowed range (0 <= bn < 2^$sizeInBits)")
    }

    val byteArray = value.toByteArray
    val bits = BitVector(byteArray)

    val littleEndian = bits.reverseByteOrder

    var unsignedBits = littleEndian
    while (unsignedBits.endsWith(BitVector.zero))
      unsignedBits = unsignedBits.dropRight(1)

    if (unsignedBits sizeGreaterThan sizeInBits) {
      throw new IllegalArgumentException(s"Big number exceeds max bits count of $sizeInBits (is ${littleEndian.size})")
    }

    val resizedVector = unsignedBits.padRight(sizeInBits)

    Attempt.successful(resizedVector)
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[BigInt]] = {
    val usableBits = bits.take(sizeInBits)
    val signedBits = usableBits ++ BitVector.lowByte

    val bigEndianBytes = signedBits.reverseByteOrder

    val bigInt = BigInt(bigEndianBytes.toByteArray)

    Attempt.successful(DecodeResult[BigInt](bigInt, bits.drop(sizeInBits)))
  }

  override def toString = s"BigIntCodec"
}
