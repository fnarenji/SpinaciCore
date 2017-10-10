package wow.realm.objects.fields.types

import scodec.Attempt
import scodec.bits.BitVector

/**
  * Created by sknz on 5/31/17.
  */
class PaddingField extends Field[Unit](Unit, FieldVisibility.ValueSet(FieldVisibility.None)) {
  private val codec = scodec.codecs.uint32L

  override def sizeInBits: Long = codec.sizeBound.exact.get

  override def serialize(initialSending: Boolean, visibility: FieldVisibility.ValueSet, s: WrittenBytesTracker): Attempt[BitVector] = {
    s.notSet(sizeInWords)
    Attempt.successful(BitVector.empty)
  }
}
