package ensiwow.auth.protocol

import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.bits.BitVector
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
package object codecs {
  /**
    * Codec that reverses String obtained by another String codec
    * @param codec string codec to be reversed
    * @return reversing codec
    */
  def reverse(codec: Codec[String]) : Codec[String] = new ReversedStringCodec(codec)

  /**
    * Codec that stops a string at the first nul occurence or until all is consumed
    */
  val fixedCString: Codec[String] = filtered(ascii, new Codec[BitVector] {
    private val nul = BitVector.lowByte
    override def sizeBound: SizeBound = SizeBound.unknown
    override def encode(bits: BitVector): Attempt[BitVector] = Attempt.successful(bits ++ nul)
    override def decode(bits: BitVector): Attempt[DecodeResult[BitVector]] = {
      bits.bytes.indexOfSlice(nul.bytes) match {
        case -1 => Attempt.successful(DecodeResult(bits, BitVector.empty))
        case i => Attempt.successful(DecodeResult(bits.take(i * 8L), bits.drop(i * 8L + 8L)))
      }
    }
  }).withToString("fixedCString")
}
