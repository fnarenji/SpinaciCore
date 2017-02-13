package ensiwow.auth.protocol.codecs

import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, SizeBound}

/**
  * Created by sknz on 2/7/17.
  */
private[codecs] final class ReversedStringCodec(stringCodec: Codec[String]) extends Codec[String] {
  override def sizeBound: SizeBound = stringCodec.sizeBound

  override def encode(value: String): Attempt[BitVector] = {
    stringCodec encode value.reverse
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[String]] = {
    val attempt = stringCodec decode bits

    attempt match {
      case Successful(DecodeResult(value, remainder)) =>
        Attempt.successful(DecodeResult[String](value.reverse, remainder))
      case _ => attempt
    }
  }

  override def toString = s"Reversed ${stringCodec}"
}
