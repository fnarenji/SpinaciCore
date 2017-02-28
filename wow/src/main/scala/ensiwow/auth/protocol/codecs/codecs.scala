package ensiwow.auth.protocol

import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, SizeBound}

/**
  * Codecs specific or practical for packet reading
  */
package object codecs {
  /**
    * Codec that reverses String obtained by another String codec
    *
    * @param codec string codec to be reversed
    * @return reversing codec
    */
  def reverse(codec: Codec[String]): Codec[String] = new ReversedStringCodec(codec)

  /**
    * Codec that stops a string at the first nul occurence or until all is consumed
    */
  def fixedCString(sizeInBytes: Long): Codec[String] = filtered(ascii, new Codec[BitVector] {
    private val nul = BitVector.lowByte

    override def sizeBound: SizeBound = SizeBound.unknown

    override def encode(bits: BitVector): Attempt[BitVector] = {
      val padded = bits.padRight(sizeInBytes * 8L)

      Attempt.successful(padded)
    }

    override def decode(bits: BitVector): Attempt[DecodeResult[BitVector]] = {
      val actualSize = bits.bytes.indexOfSlice(nul.bytes) match {
        case i if i <= sizeInBytes => i
        case _ => sizeInBytes
      }

      val take = bits.take(actualSize * 8L)
      val drop = bits.drop(sizeInBytes * 8L)
      Attempt.successful(DecodeResult(take, drop))
    }
  }).withToString("fixedCString")

  /**
    * Converts an instance of T to constant codec
    *
    * @param t     expected constant
    * @param codec codec to encode constant
    * @tparam T type of constant
    * @return constant codec
    */
  def constantE[T](t: T)(implicit codec: Codec[T]): Codec[Unit] = constant(codec.encode(t).require)

  /**
    * Codec for big integers
    */
  def fixedUBigIntL(sizeInBytes: Long): Codec[BigInt] = new FixedUnsignedLBigIntCodec(sizeInBytes)

  /**
    * Codec for decoding a vector that is prefixed by its size
    * @param sizeCodec size codec
    * @param valueCodec vector element codec
    * @tparam T type of element
    * @return size prefixed vector codec
    */
  def variableSizeVector[T](sizeCodec: Codec[Int], valueCodec: Codec[T]): Codec[Vector[T]] = new VariableSizeVector[T](
    sizeCodec,
    valueCodec)
}
