package ensiwow.common.codecs

import org.scalatest.{Assertion, Assertions, Matchers}
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult}
import scodec.bits.BitVector

/**
  * Created by sknz on 2/28/17.
  */
object CodecTestUtils extends Assertions with Matchers {
  def decode[T](bits: BitVector, expectedValue: T)(implicit codec: Codec[T]): Assertion = {
    codec.decode(bits) match {
      case Successful(DecodeResult(value, BitVector.empty)) =>
        value shouldEqual expectedValue
      case Successful(DecodeResult(value, remainder)) => fail(s"non empty remainder: $value / $remainder")
      case Failure(err) => fail(err.toString())
    }
  }

  def encode[T](expectedBits: BitVector, value: T)(implicit codec: Codec[T]): Assertion = {
    codec.encode(value) match {
      case Successful(bits) => bits shouldEqual expectedBits
      case Failure(err) => fail(err.toString())
    }
  }

}
