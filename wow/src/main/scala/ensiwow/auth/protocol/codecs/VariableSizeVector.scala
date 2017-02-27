package ensiwow.auth.protocol.codecs

import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import scodec.codecs._

/**
  * Encodes a unsigned big integer in a fixed number of bytes
  */
private[codecs] final class VariableSizeVector[T](sizeCodec: Codec[Int], valueCodec: Codec[T]) extends Codec[Vector[T]] {
  override def sizeBound: SizeBound = sizeCodec.sizeBound + valueCodec.sizeBound

  override def encode(values: Vector[T]): Attempt[BitVector] = {
    for {
      encSize <- sizeCodec.encode(values.size)
      encVector <- vectorOfN(provide(values.size), valueCodec).encode(values)
    } yield encSize ++ encVector
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[Vector[T]]] = {
     sizeCodec.decode(bits) match {
       case Successful(DecodeResult(size, vectorBits)) =>
         vectorOfN(provide(size), valueCodec).decode(vectorBits)
       case f@Failure(_) => f
     }
  }

  override def toString = s"BigIntCodec"
}
