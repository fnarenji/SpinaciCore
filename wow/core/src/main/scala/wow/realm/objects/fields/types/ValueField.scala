package wow.realm.objects.fields.types

import scodec.Attempt.Successful
import scodec.bits.BitVector
import scodec.{Attempt, Codec}

/**
  * Created by sknz on 5/31/17.
  */
class ValueField[A](override val init: A, visibleBy: FieldVisibility.ValueSet)(implicit codec: Codec[A])
  extends Field[A](init, visibleBy) {
  require(codec.sizeBound.exact.isDefined)

  protected def writeIfNonZero(s: WrittenBytesTracker): Attempt[BitVector] = codec.encode(value) match {
    case success@Successful(bits) =>
      // Don't write if only zeroes
      if (bits == BitVector.low(bits.size)) {
        s.notSet(sizeInWords)
        Attempt.successful(BitVector.empty)
      } else {
        s.set(sizeInWords)
        success
      }
    case f => f
  }

  override def serialize(
    initialSending: Boolean,
    visibility: FieldVisibility.ValueSet,
    s: WrittenBytesTracker): Attempt[BitVector] = {
    if (visibleBy.intersect(visibility).nonEmpty && initialSending) {
      writeIfNonZero(s)
    } else {
      s.notSet(sizeInWords)
      Attempt.successful(BitVector.empty)
    }
  }

  override def sizeInBits: Long = codec.sizeBound.exact.get
}
