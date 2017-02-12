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
      bits.bytes.indexOfSlice(nul.bytes) match {
        case i if i <= sizeInBytes =>
          val take = bits.take(i * 8L)
          val drop = bits.drop(sizeInBytes * 8L)
          Attempt.successful(DecodeResult(take, drop))
        case _ =>
          val take = bits.take(sizeInBytes * 8L)
          val drop = bits.drop(sizeInBytes * 8L)
          Attempt.successful(DecodeResult(take, drop))
      }
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
  def fixedUBigIntL(sizeInBytes: Long): Codec[BigInt] = new FixedUnsignedBigIntCodec(sizeInBytes)
}
