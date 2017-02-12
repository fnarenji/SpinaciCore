package ensiwow.auth.protocol.codecs

import org.scalatest.{FlatSpec, Matchers}
import scodec.Attempt.{Failure, Successful}
import scodec.DecodeResult
import scodec.bits._

/**
  * Created by sknz on 2/12/17.
  */
class FixedUnsignedBigIntCodecTest extends FlatSpec with Matchers {
  "A FixedUnsignedBigIntCodec" must "fail on negative number" in {
    val codec = fixedUBigIntL(32)
    val value = BigInt("-983129873182379123791298731289319723712798318927379123725")

    val encode = codec.encode(value)
    assert(encode.isFailure)
  }

  it must "fail on over-capacity number" in {
    val codec = fixedUBigIntL(4)
    val value = BigInt(Long.MaxValue)

    val encode = codec.encode(value)
    assert(encode.isFailure)
  }

  it must "succeed on max number" in {
    val codec = fixedUBigIntL(4)
    val value = BigInt(0xFFFFFFFFL)

    val attempt = codec.encode(value)

    attempt match {
      case Failure(err) => fail(err.toString())
      case Successful(bits) =>
        bits shouldEqual hex"FFFFFFFF".bits

        val decode = codec.decode(bits)
        decode match {
          case Successful(DecodeResult(decoded, BitVector.empty)) => decoded shouldEqual value
          case Successful(DecodeResult(_, remainder)) => fail(s"non empty remainder: $remainder")
          case Failure(err) => fail(err.toString())
        }
    }
  }

  it must "succeed on in range number" in {
    val codec = fixedUBigIntL(4)
    val value = BigInt(0xFFFFL)

    val attempt = codec.encode(value)

    attempt match {
      case Failure(err) => fail(err.toString())
      case Successful(bits) =>
        bits shouldEqual hex"FFFF0000".bits

        val decode = codec.decode(bits)
        decode match {
          case Successful(DecodeResult(decoded, BitVector.empty)) => decoded shouldEqual value
          case Successful(DecodeResult(_, remainder)) => fail(s"non empty remainder: $remainder")
          case Failure(err) => fail(err.toString())
        }
    }
  }

  it must "succeed on zero" in {
    val codec = fixedUBigIntL(4)
    val value = BigInt(0)

    val attempt = codec.encode(value)

    attempt match {
      case Failure(err) => fail(err.toString())
      case Successful(bits) =>
        bits shouldEqual hex"00000000".bits

        val decode = codec.decode(bits)
        decode match {
          case Successful(DecodeResult(decoded, BitVector.empty)) => decoded shouldEqual value
          case Successful(DecodeResult(_, remainder)) => fail(s"non empty remainder: $remainder")
          case Failure(err) => fail(err.toString())
        }
    }
  }
}
