package wow.realm.objects.fields

import scodec.codecs._
import shapeless.Generic
import wow.realm.objects.{Guid, GuidType}
import wow.realm.objects.fields.types._

/**
  * Created by sknz on 5/31/17.
  */
case class ObjectFields private(
  guid: ValueField[Guid],
  tpe: ValueField[TypeMask.ValueSet],
  entry: ValueField[Long],
  scale: VariableValueField[Float],
  padding: PaddingField
) {
  require(scale() >= 0.0)
}

object ObjectFields {
  val generic = Generic[ObjectFields]
  type Repr = generic.Repr

  def apply(guid: Guid, tpe: TypeMask.ValueSet, entry: Long, scale: Float): ObjectFields = {
    import wow.realm.objects.fields.types.FieldVisibility._

    val o = ObjectFields(
      guid.toValField(Guid.codec, Public),
      tpe.toValField(Public),
      entry.toValField(uint32L, Public),
      scale.toVarField(floatL, Public),
      new PaddingField
    )

    o
  }

  def main(args: Array[String]): Unit = {
    val guid = Guid(16777215, GuidType.Character)
    val tpe = TypeMask.ValueSet()
    val entry = 10L
    val scale = 0f

    val c = ObjectFields(guid, tpe, entry, scale).toCompoundField

    val s = new WrittenBytesTracker(c.sizeInWords)
    val r = c.serialize(initialSending = true, FieldVisibility.values, s)

    println(s.finish().toBin)
    println(r)
  }
}