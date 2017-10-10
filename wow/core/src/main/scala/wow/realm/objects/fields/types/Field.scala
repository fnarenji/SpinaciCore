package wow.realm.objects.fields.types

import scodec.Attempt
import scodec.bits.BitVector

/**
  * Created by sknz on 5/31/17.
  */
abstract class Field[A](val init: A, visibleBy: FieldVisibility.ValueSet) {
  protected var value: A = init

  def apply(): A = value

  def sizeInBits: Long
  def sizeInWords: Long = sizeInBits / 8 / 4
  def serialize(initialSending: Boolean, visibility: FieldVisibility.ValueSet, s: WrittenBytesTracker): Attempt[BitVector]
}


