package wow.realm.objects.fields.types

import scodec.bits.BitVector
import scodec.{Attempt, Codec}

/**
  * Created by sknz on 5/31/17.
  */
class VariableValueField[A](override val init: A, visibleBy: FieldVisibility.ValueSet)(implicit codec: Codec[A])
  extends ValueField[A](init, visibleBy)(codec) {
  // TODO: This should be set to true after every change and reset after it's been dispatched to all concerned players
  private var dirty = false

  def apply(newValue: A): Unit = {
    if (value != newValue) {
      dirty = true
      value = newValue
    }
  }

  override def serialize(initialSending: Boolean, visibility: FieldVisibility.ValueSet, s: WrittenBytesTracker): Attempt[BitVector] =  {
    val canSee = visibleBy.intersect(visibility).nonEmpty

    if (canSee && initialSending) {
      writeIfNonZero(s)
    } else if (canSee && !initialSending && dirty) {
      s.set(sizeInWords)
      codec.encode(value)
    } else {
      s.notSet(sizeInWords)
      Attempt.successful(BitVector.empty)
    }
  }
}
