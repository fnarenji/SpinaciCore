package wow.realm.objects.fields.types

/**
  * Created by sknz on 5/31/17.
  */
object FieldVisibility extends Enumeration {
  val None = Value(0x000)
  val Public = Value(0x001)
  val Private = Value(0x002)
  val Owner = Value(0x004)
  val Unused1 = Value(0x008)
  val ItemOwner = Value(0x010)
  val SpecialInfo = Value(0x020)
  val PartyMember = Value(0x040)
  val Unused2 = Value(0x080)
  val Dynamic = Value(0x100)
  val NotSpecified = Value
}
