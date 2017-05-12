package wow.common.codecs

import org.scalatest.{FlatSpec, Matchers}
import scodec.bits.{BitVector, _}
import scodec.codecs._

/**
  * Variable size vector codec test
  */
class VariableSizeSeqTest extends FlatSpec with Matchers {
  behavior of "VariableSizeVectorTest"

  private val sizeBits = 16
  private implicit val codec = sizePrefixedSeq(uintL(sizeBits), uint32L).upcast[Seq[Long]]

  private val emptyVector = Seq[Long]()
  private val emptyVectorBits = BitVector.low(sizeBits)
  it must "correctly decode an empty uint sized vector" in CodecTestUtils.decode(emptyVectorBits, emptyVector)
  it must "correctly encode an empty uint sized vector" in CodecTestUtils.encode(emptyVectorBits, emptyVector)

  private val referenceVector = Seq[Long](1, 2, 3, 4)
  private val referenceVectorBits = hex"040001000000020000000300000004000000".bits
  it must "correctly decode a vector" in CodecTestUtils.decode(referenceVectorBits, referenceVector)
  it must "correctly encode a vector" in CodecTestUtils.encode(referenceVectorBits, referenceVector)
}
