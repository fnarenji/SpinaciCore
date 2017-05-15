package wow.common

import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Decoder, Encoder, Err, SizeBound}
import wow.realm.protocol.objectupdates.UpdateFlags

import scala.collection.{immutable, mutable}
import scala.language.postfixOps

/**
  * Codecs specific or practical for packet reading
  */
package object codecs {
  /**
    * Codec that reverses String obtained by another String codec
    *
    * @param codec string codec to be reversed
    * @return reversing codec */
  def reverse(codec: Codec[String]): Codec[String] = new ReversedStringCodec(codec)

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
    * Converts an instance of A to constant codec
    *
    * @param c     expected constant
    * @param codec codec to encode constant
    * @tparam A type of constant
    * @return constant codec
    */
  def constantE[A](c: A)(implicit codec: Codec[A]): Codec[Unit] = constant(codec.encode(c).require)

  /**
    * Codec for big integers
    */
  def fixedUBigIntL(sizeInBytes: Long): Codec[BigInt] = new FixedUnsignedLBigIntCodec(sizeInBytes)

  /**
    * Codec for sequence
    *
    * @param codec codec for sequence element type
    * @tparam A element type
    * @return sequence codec
    */
  def seqCodec[A](codec: Codec[A], readLimit: Option[Int]): Codec[immutable.Seq[A]] = new Codec[immutable.Seq[A]] {
    def sizeBound: SizeBound = SizeBound.unknown

    def encode(seq: immutable.Seq[A]): Attempt[BitVector] = Encoder.encodeSeq(codec)(seq)

    def decode(buffer: BitVector): Attempt[DecodeResult[immutable.Seq[A]]] = Decoder.decodeCollect[immutable.Seq, A](
      codec,
      readLimit)(buffer)

    override def toString = s"seq($codec)"
  }

  /**
    * Codec for vector that is prefixed by its size
    *
    * @param sizeCodec  size codec
    * @param valueCodec vector element codec
    * @tparam A type of element
    * @return size prefixed vector codec
    */
  def sizePrefixedSeq[A](sizeCodec: Codec[Int], valueCodec: Codec[A]): Codec[immutable.Seq[A]] =
    sizeCodec.consume[immutable.Seq[A]](size => {
      val codec: Codec[immutable.Seq[A]] = seqCodec(valueCodec, Some(size))

      codec.exmap[immutable.Seq[A]](seq => {
        if (seq.size == size) {
          Attempt.successful(seq)
        } else {
          Attempt
            .failure(Err(s"Insufficient number of elements: decoded ${seq.size} but should have decoded $size"))
        }
      }, Attempt.successful).withToString(s"sizePrefixedSeq($sizeCodec, $valueCodec)")
    })(_.size)

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
  def sizePrefixedTransform[A](
    sizeCodec: Codec[Long],
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
    * @tparam A value type
    * @return bound checking codec
    */
  def upperBound[A: Ordering](codec: Codec[A], upperBound: A): Codec[A] = {
    def boundsCheck(value: A) = {
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
    *
    * @param size size in bits
    * @return c style boolean codec
    */
  def cbool(size: Long): Codec[Boolean] = ignore(size - 1).dropLeft(bool(1))

  /**
    * Not empty codec only read/writes a value if it's present.
    * Reading works by checking if enough bits are available, thus this codec should most likely only be used for values
    * at the end.
    *
    * @param codec value codec to read
    * @tparam A type of value
    * @return codec of optional A
    */
  def optionalRemainder[A](codec: Codec[A]): Codec[Option[A]] = new Codec[Option[A]] {
    override def sizeBound: SizeBound = SizeBound.atLeast(0) | codec.sizeBound

    override def decode(bits: BitVector): Attempt[DecodeResult[Option[A]]] = {
      if (bits.nonEmpty) {
        codec.decode(bits) match {
          case Successful(DecodeResult(value, remainder)) =>
            Attempt.successful(DecodeResult(Some(value), remainder))
          case f: Failure =>
            f
        }
      } else {
        Attempt.successful(DecodeResult(None, BitVector.empty))
      }
    }

    override def encode(value: Option[A]): Attempt[BitVector] = {
      value match {
        case Some(actualValue) =>
          codec.encode(actualValue)
        case None =>
          Attempt.successful(BitVector.empty)
      }
    }
  }

  /**
    * Narrows a Long codec to an Int.
    *
    * @param codec long codec
    * @return narrowed codec, will fail if value can't be narrowed
    */
  def long2Int(codec: Codec[Long]): Codec[Int] = {
    codec.narrow(l => {
      if (l.isValidInt) {
        Attempt.successful(l.toInt)
      } else {
        Attempt.failure(Err("Long value did not belong to Int range"))
      }
    }, i => i.toLong)
  }

  /**
    * Fixes a value, not written nor read to stream
    *
    * @param value value to fix
    * @tparam A type of value
    * @return fixed codec
    */
  def fixed[A](value: A): Codec[A] = new Codec[A] {
    override def decode(bits: BitVector): Attempt[DecodeResult[A]] = Attempt.Successful(DecodeResult(value, bits))

    override def encode(value: A): Attempt[BitVector] = Attempt.Successful(BitVector.empty)

    override def sizeBound: SizeBound = SizeBound.exact(0)
  }

  /**
    * Encode a set of flags as a fixed length bitmask
    *
    * @param e     enum object
    * @param codec codec for bitmask
    * @tparam A enumeration type
    * @tparam I integer type for bitmask
    * @return bitmask codec
    */
  def fixedBitmask[A <: Enumeration, I](e: A, codec: Codec[I])
    (implicit numeric: Integral[I]): Codec[e.ValueSet] = new Codec[e.ValueSet] {

    import numeric._

    private type ValueSet = e.ValueSet
    private val ValueSet = e.ValueSet

    require(UpdateFlags.values.max.id <= math.pow(2L, codec.sizeBound.exact.get - 1L).toInt)

    override def decode(bits: BitVector): Attempt[DecodeResult[ValueSet]] = {
      codec.decode(bits) match {
        case Successful(DecodeResult(mask, rem)) =>
          val builder = ValueSet.newBuilder

          for (elem <- e.values) {
            if ((elem.id & mask.toInt()) > 0) {
              builder += elem
            }
          }

          val result = builder.result()

          Attempt.Successful(DecodeResult(result, rem))

        case f: Failure => f
      }
    }

    override def encode(valueSet: ValueSet): Attempt[BitVector] = {
      var mask = 0

      for (elem <- valueSet) {
        mask = mask | elem.id
      }

      codec.encode(numeric.fromInt(mask))
    }

    override def sizeBound: SizeBound = codec.sizeBound
  }

  /**
    * Treats an Option[A] as if it was required
    *
    * @param codec codec for A
    * @tparam A type of optional
    * @return Option[A] codec that is not optional
    */
  def requiredOptional[A](codec: Codec[A]): Codec[Option[A]] = {
    codec.exmap(v => Attempt.Successful(Some(v)), o => o match {
      case Some(v) =>
        Attempt.Successful(v)
      case None =>
        Attempt.Failure(Err("No value"))
    })
  }

  /**
    * Prefixes the output of the codec by a bitmask indicating which bytes are non null, and zero packs null bytes
    *
    * @param size  bitmask size
    * @param codec codec to filter
    * @tparam A type of codec
    * @return zero packing codec for type A
    */
  def zeroPacked[A](size: Int, codec: Codec[A]): Codec[A] = filtered(codec, new Codec[BitVector] {
    override def decode(bits: BitVector): Attempt[DecodeResult[BitVector]] = {
      bits.acquireThen(size)(x => Attempt.failure(Err(x)), maskBits => {
        val reversedMask = maskBits.reverseBitOrder

        var packedBits = bits.drop(size)
        var unpackedBits = BitVector.low(size * 8)

        val errors = mutable.Buffer[String]()
        for (i <- 0 until size if reversedMask(i)) {
          packedBits.acquire(8) match {
            case Left(error) =>
              errors.append(error)
            case Right(unpackedByte) =>
              packedBits = packedBits.drop(8)
              unpackedBits = unpackedBits.patch(i * 8, unpackedByte)
          }
        }

        if (errors.isEmpty) {
          Attempt.Successful(DecodeResult(unpackedBits, packedBits))
        } else {
          Attempt.failure(Err(errors mkString "\n"))
        }
      })
    }

    override def encode(value: BitVector): Attempt[BitVector] = {
      val bytes = value.toByteArray

      assert(bytes.length == size)
      val nonZeroByteIndices = bytes.zipWithIndex.filter { case (b, _) => b != 0.toByte }.map(_._2)

      var mask = BitVector.low(size)
      for (elem <- nonZeroByteIndices) {
        mask = mask.set(elem)
      }
      mask = mask.reverseBitOrder

      var packedData = mask.bytes

      for (i <- nonZeroByteIndices) {
        packedData = packedData ++ ByteVector(bytes(i))
      }

      Attempt.successful(packedData.bits)
    }

    override def sizeBound: SizeBound = SizeBound.bounded(size, (size + 1) * 8)
  })
}
