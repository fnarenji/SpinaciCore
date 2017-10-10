package wow.realm.objects.fields.types

import scodec.Attempt.Failure
import scodec.bits.BitVector
import scodec.{Attempt, Err}
import shapeless.ops.hlist.ToTraversable
import shapeless.{Generic, HList}

/**
  * Created by sknz on 5/31/17.
  */
class CompoundField[A, Repr <: HList](override val init: A, visibleBy: FieldVisibility.ValueSet)
  (implicit gen: Generic.Aux[A, Repr], toTraversable: ToTraversable.Aux[Repr, List, Field[_]])
  extends Field[A](init, visibleBy) {

  private val tuple = gen.to(value)

  private val fields: List[Field[_]] = tuple.toList[Field[_]](toTraversable)

  override def serialize(initialSending: Boolean, visibility: FieldVisibility.ValueSet, s: WrittenBytesTracker): Attempt[BitVector] = {
    val hasVisibilityOverride = !visibleBy.contains(FieldVisibility.NotSpecified)

    if (hasVisibilityOverride && visibleBy.intersect(visibility).nonEmpty || !hasVisibilityOverride) {
      fields.map(f => f.serialize(initialSending, visibility, s)).partition(_.isFailure) match {
        case (Nil, bitVectors) =>
          Attempt.Successful(bitVectors.map(_.require).foldLeft(BitVector.empty) { case (acc, bits) => acc ++ bits })
        case (failures, _) =>
          Attempt.failure(Err(failures.collect { case Failure(err) => err.toString() }.mkString("\n")))
      }
    } else {
      s.notSet(sizeInWords)
      Attempt.successful(BitVector.empty)
    }
  }

  override def sizeInBits: Long = fields.map(_.sizeInBits).sum
}
