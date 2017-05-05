package ensiwow.common.codecs

import org.scalatest.{Assertion, Assertions, Matchers}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

/**
  * Simple unit test helpers for serialization
  */
object CodecTestUtils extends Assertions with Matchers {
  def decode[A](bits: BitVector, expectedValue: A)(implicit codec: Codec[_ >: A]): Assertion = {
    codec.decode(bits) match {
      case Successful(DecodeResult(x, remainder)) if remainder.nonEmpty =>
        fail(s"non empty remainder: $value / $remainder")
      case Successful(DecodeResult(value, BitVector.empty)) =>
        value shouldEqual expectedValue
      case Failure(err) => fail(err.toString())
    }
  }

  def encode[A](expectedBits: BitVector, value: A)(implicit codec: Codec[_ >: A]): Assertion = {
    codec.encode(value) match {
      case Successful(bits) =>
        bits.toHex shouldEqual expectedBits.toHex
        bits shouldEqual expectedBits
      case Failure(err) => fail(err.toString())
    }
  }

}
