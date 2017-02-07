package ensiwow.auth.protocol.codecs

import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import scodec.bits.BitVector

/**
  * Created by sknz on 2/7/17.
  */
private[codecs] class ReversedStringCodec(stringCodec : Codec[String]) extends Codec[String]{
  override def sizeBound: SizeBound = stringCodec.sizeBound

  override def encode(value: String): Attempt[BitVector] = {
    stringCodec encode value.reverse
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[String]] = {
    val attempt = stringCodec decode bits

    // If we have a successful attempt, reverse string and return reversed string
    if (attempt.isSuccessful) {
      val result = attempt.require

      val reversedValue = result.value.reverse
      Attempt.successful(DecodeResult[String](reversedValue, result.remainder))
    } else {
      attempt
    }
  }

  override def toString = s"Reversed ${stringCodec}"
}
