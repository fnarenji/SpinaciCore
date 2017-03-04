package ensiwow.common

import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

/**
  * Codecs specific or practical for packet reading
  */
package object codecs {
  /**
    * Codec that reverses String obtained by another String codec
    *
    * @param codec string codec to be reversed
    * @return reversing codec */ def reverse(codec: Codec[String]): Codec[String] = new ReversedStringCodec(codec)

  /**
    * Codec that stops a string at the first nul occurrence or until all is consumed
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
    *
    * @param sizeCodec  size codec
    * @param valueCodec vector element codec
    * @tparam T type of element
    * @return size prefixed vector codec
    */
  def variableSizeVector[T](sizeCodec: Codec[Int], valueCodec: Codec[T]): Codec[Vector[T]] =
    sizeCodec.consume(size => vectorOfN(provide(size), valueCodec))(_.size)

  /**
    * Server packet size codec
    * One-bit boolean prefix determines big endian uint width (0 -> 15, 1 -> 23) and then int is read
    *
    * @return codec for server packet size
    */
  val serverPacketSizeCodec: Codec[Int] = {
    val SmallSize = 15
    val BigSize = 23
    val Boundary = math.pow(2, 15).toInt

    def codecProvider(isBig: Boolean): Codec[Int] = uint(if (isBig) {
      BigSize
    } else {
      SmallSize
    })

    bool(1).consume[Int](codecProvider)(_ >= Boundary)
  }

  /**
    * Add integer offset to integer codec
    *
    * @param codec  integer codec to offset
    * @param offset offset
    * @return integer codec with offset
    */
  def integerOffset(codec: Codec[Int], offset: Int): Codec[Int] = codec.xmap[Int](_ + offset, _ - offset)

  /**
    * Prefixes the encoded date with it's size in bytes before applying the transform
    *
    * @param sizeCodec size codec
    * @param codec     value codec
    * @tparam A value type
    * @return byte size prefixed codec
    */
  def sizePrefixedTransform[A](sizeCodec: Codec[Long],
                               codec: Codec[A],
                               transform: Codec[BitVector]): Codec[A] = new Codec[A] {
    override def sizeBound: SizeBound = codec.sizeBound + sizeCodec.sizeBound

    override def encode(value: A): Attempt[BitVector] = {
      for {
        valueBits <- codec encode value
      } yield {
        for {
          sizeBits <- sizeCodec encode valueBits.bytes.length
          transformedBits <- transform encode valueBits
        } yield {
          sizeBits ++ transformedBits
        }
      }
    }.flatten

    override def decode(bits: BitVector): Attempt[DecodeResult[A]] = {
      sizeCodec.decode(bits) map {
        case DecodeResult(valueSize, remainder) =>
          transform.decode(remainder) map { case DecodeResult(valueBits, _) =>
            fixedSizeBytes(valueSize, codec).decode(valueBits)
          } flatten
      } flatten
    }
  }

  /**
    * Upper bound checking codec
    *
    * @param codec      value codec
    * @param upperBound upper bound
    * @tparam T value type
    * @return bound checking codec
    */
  def upperBound[T: Ordering](codec: Codec[T], upperBound: T): Codec[T] = {
    def boundsCheck(value: T) = {
      import Ordering.Implicits._

      if (value <= upperBound) {
        Attempt successful value
      } else {
        Attempt failure Err("Out of bounds value")
      }
    }

    codec.exmap(boundsCheck, boundsCheck)
  }


  /**
    * C style boolean (all zeros and then 1-bit)
    * @param size size in bits
    * @return c style boolean codec
    */
  def cbool(size: Long): Codec[Boolean] = ignore(size - 1).dropLeft(bool(1))
}
