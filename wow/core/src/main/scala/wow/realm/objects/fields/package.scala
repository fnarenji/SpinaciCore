package wow.realm.objects

import scodec.Codec
import shapeless.ops.hlist.ToTraversable
import shapeless.{Generic, HList}
import wow.realm.objects.fields.types._

package object fields {

  implicit class Compoundable[A](val self: A) extends AnyVal {
    def toCompoundField[Repr <: HList](fieldVisibility: FieldVisibility.Value*)(
      implicit gen: Generic.Aux[A, Repr], toTraversable: ToTraversable.Aux[Repr, List, Field[_]]) =
    new CompoundField[A, Repr](self, FieldVisibility.ValueSet(fieldVisibility: _*))

    def toCompoundField[Repr <: HList](
      implicit gen: Generic.Aux[A, Repr],
      toTraversable: ToTraversable.Aux[Repr, List, Field[_]]): CompoundField[A, Repr] =
      self.toCompoundField(FieldVisibility.NotSpecified)

    def toValField(fieldVisibility: FieldVisibility.Value*)(implicit codec: Codec[A]): ValueField[A] =
      new ValueField[A](self, FieldVisibility.ValueSet(fieldVisibility:_*))

    def toValField(codec: Codec[A], fieldVisibility: FieldVisibility.Value*): ValueField[A] =
      new ValueField[A](self, FieldVisibility.ValueSet(fieldVisibility:_*))(codec)

    def toVarField(fieldVisibility: FieldVisibility.Value*)(implicit codec: Codec[A]): VariableValueField[A] =
      new VariableValueField[A](self, FieldVisibility.ValueSet(fieldVisibility:_*))

    def toVarField(codec: Codec[A], fieldVisibility: FieldVisibility.Value*): VariableValueField[A] =
      new VariableValueField[A](self, FieldVisibility.ValueSet(fieldVisibility:_*))(codec)
  }
}
