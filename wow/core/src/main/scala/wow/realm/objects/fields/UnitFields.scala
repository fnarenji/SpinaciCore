package wow.realm.objects.fields

import scodec.Codec
import shapeless.ops.nat.ToInt
import shapeless.{Generic, Nat, Sized}
import wow.realm.objects.Guid
import wow.realm.objects.fields.types.{CompoundField, PaddingField, VariableValueField}

class FixedArray

import shapeless.nat._
import shapeless.syntax.sized._

case class UnitFields private(
  objectFields: CompoundField[ObjectFields, ObjectFields.Repr],
  charm: VariableValueField[Guid],
  summon: VariableValueField[Guid],
  critter: VariableValueField[Guid],
  charmedBy: VariableValueField[Guid],
  summonedBy: VariableValueField[Guid],
  createdBy: VariableValueField[Guid],
  target: VariableValueField[Guid],
  channelObject: VariableValueField[Guid],
  channelSpell: VariableValueField[Long],
  bytes0: VariableValueField[Sized[Vector[Byte], _4]],
  health: VariableValueField[Long],
  powers: VariableValueField[Sized[Vector[Long], _7]],
  maxPowers: VariableValueField[Sized[Vector[Long], _7]],

  powerRegenerationFlatModifiers: VariableValueField[Sized[Vector[Float], _7]],
 powerRegenerationLongerruptedFlatModifiers: VariableValueField[Sized[Vector[Float], _7]],
  level: VariableValueField[Long],
  factionTemplate: VariableValueField[Long],
  virtualItemSlotId: VariableValueField[Sized[Vector[Long], _3]],
  flags1: VariableValueField[Int],
  flags2: VariableValueField[Int],
  baseAttackTime: VariableValueField[Sized[Vector[Long], _2]],
  rangedAttackTime: VariableValueField[Long],
  boundingRadius: VariableValueField[Float],
  combatReach: VariableValueField[Float],
  displayId: VariableValueField[Long],
  nativeDisplayId: VariableValueField[Long],
  mountDisplayId: VariableValueField[Long],
  minDamage: VariableValueField[Float],
  maxDamage: VariableValueField[Float],
  minOffhandDamage: VariableValueField[Float],
  maxOffhandDamage: VariableValueField[Float],
  bytes1: VariableValueField[Sized[Vector[Byte], _4]],
  petNumber: VariableValueField[Long],
  petNameTimestamp: VariableValueField[Long],
  petExperience: VariableValueField[Long],
  petNextLevelExperience: VariableValueField[Long],
  dynamicFlags: VariableValueField[Int],
  modCastSpeed: VariableValueField[Float],
  createdBySpell: VariableValueField[Long],
  npcFlags: VariableValueField[Int],
  emoteState: VariableValueField[Long],
  stats: VariableValueField[Sized[Vector[Long], _5]],
  positiveStats: VariableValueField[Sized[Vector[Long], _5]],
  negativeStats: VariableValueField[Sized[Vector[Long], _5]],
  resistances: VariableValueField[Sized[Vector[Long], _7]],
  resistanceBuffModifierPositive: VariableValueField[Sized[Vector[Long], _7]],
  resistanceBuffModifierNegative: VariableValueField[Sized[Vector[Long], _7]],
  baseMana: VariableValueField[Long],
  baseHealth: VariableValueField[Long],
  bytes2: VariableValueField[Sized[Vector[Byte], _4]],
  attackPower: VariableValueField[Long],
  attackPowerMods: VariableValueField[(Int, Int)],
  attackPowerMultiplier: VariableValueField[Float],
  minRangedDamage: VariableValueField[Float],
  maxRangedDamage: VariableValueField[Float],
  powerCostModifiers: VariableValueField[Sized[Vector[Float], _7]],
  powerCostMultiplier: VariableValueField[Sized[Vector[Float], _7]],
  maxHealthModifier: VariableValueField[Float],
  hoverHeight: VariableValueField[Float],
  padding: PaddingField
)

object UnitFields {
  val generic = Generic[UnitFields]

  implicit def fixedSizeT[A, N <: Nat](codec: Codec[A])(implicit toInt: ToInt[N]): Codec[Sized[Vector[A], N]] = {
    val size = Nat.toInt[N]

    import scodec._
    import scodec.codecs._

    scodec.codecs.vectorOfN(provide(size), codec).exmap[Sized[Vector[A], N]]({
      v =>
        v.sized[N] match {
          case Some(sized) => Attempt.successful(sized)
          case None => Attempt.failure(Err("Mis-sized vector"))
        }
    }, {
      s =>
        Attempt.successful(s.unsized)
    })
  }

  type Repr = generic.Repr

  def apply(
    objectFields: ObjectFields,
    charm: Guid,
    summon: Guid,
    critter: Guid,
    charmedBy: Guid,
    summonedBy: Guid,
    createdBy: Guid,
    target: Guid,
    channelObject: Guid,
    channelSpell: Long,
    bytes0: Sized[Vector[Byte], _4],
    health: Long,
    powers: Sized[Vector[Long], _7],
    maxPowers: Sized[Vector[Long], _7],
    powerRegenerationFlatModifiers: Sized[Vector[Float], _7],
    powerRegenerationInterruptedFlatModifiers: Sized[Vector[Float], _7],
    level: Long,
    factionTemplate: Long,
    virtualItemSlotId: Sized[Vector[Long], _3],
    flags1: Int,
    flags2: Int,
    baseAttackTime: Sized[Vector[Long], _2],
    rangedAttackTime: Long,
    boundingRadius: Float,
    combatReach: Float,
    displayId: Long,
    nativeDisplayId: Long,
    mountDisplayId: Long,
    minDamage: Float,
    maxDamage: Float,
    minOffhandDamage: Float,
    maxOffhandDamage: Float,
    bytes1: Sized[Vector[Byte], _4],
    petNumber: Long,
    petNameTimestamp: Long,
    petExperience: Long,
    petNextLevelExperience: Long,
    dynamicFlags: Int,
    modCastSped: Float,
    createdBySpell: Long,
    npcFlags: Int,
    emoteState: Long,
    stats: Sized[Vector[Long], _5],
    positiveStats: Sized[Vector[Long], _5],
    negativeStats: Sized[Vector[Long], _5],
    resistances: Sized[Vector[Long], _7],
    resistanceBuffModifierPositive: Sized[Vector[Long], _7],
    resistanceBuffModifierNegative: Sized[Vector[Long], _7],
    baseMana: Long,
    baseHealth: Long,
    bytes2: Sized[Vector[Byte], _4],
    attackPower: Long,
    attackPowerMods: (Int, Int),
    attackPowerMultiplier: Float,
    minRangedDamage: Float,
    maxRangedDamage: Float,
    powerCostModifiers: Sized[Vector[Float], _7],
    powerCostMultiplier: Sized[Vector[Float], _7],
    maxHealthModifier: Float,
    hoverHeight: Float
  ): UnitFields = {
    import scodec.codecs._
    import wow.realm.objects.fields.types.FieldVisibility._

    UnitFields(
      objectFields.toCompoundField[ObjectFields.Repr](),
      charm.toVarField(Guid.codec, Public),
      summon.toVarField(Guid.codec, Public),
      critter.toVarField(Guid.codec, Private),
      charmedBy.toVarField(Guid.codec, Public),
      summonedBy.toVarField(Guid.codec, Public),
      createdBy.toVarField(Guid.codec, Public),
      target.toVarField(Guid.codec, Public),
      channelObject.toVarField(Guid.codec, Public),
      channelSpell.toVarField(uint32L, Public),
      bytes0.toVarField(fixedSizeT[Byte, _4](byte), Public),
      health.toVarField(uint32L, Public),
      powers.toVarField(uint32L, Public),
      maxPowers.toVarField(uint32L, Public),
      powerRegenerationFlatModifiers.toVarField(fixedSizeT[Float, _7](floatL), Private, Owner),
      powerRegenerationInterruptedFlatModifiers.toVarField(fixedSizeT[Float, _7](floatL), Private, Owner),
      level.toVarField(uint32L, Public),
      factionTemplate.toVarField(uint32L, Public),
      virtualItemSlotId.toVarField(uint32L, Public),
      flags1.toVarField(int32L, Public),
      flags2.toVarField(int32L, Public),
      baseAttackTime.toVarField(uint32L, Public),
      rangedAttackTime.toVarField(uint32L, Private),
      boundingRadius.toVarField(floatL, Public),
      combatReach.toVarField(floatL, Public),
      displayId.toVarField(uint32L, Public),
      nativeDisplayId.toVarField(uint32L, Public),
      mountDisplayId.toVarField(uint32L, Public),
      minDamage.toVarField(floatL, Private, Owner, PartyMember),
      maxDamage.toVarField(floatL, Private, Owner, PartyMember),
      minOffhandDamage.toVarField(floatL, Private, Owner, SpecialInfo),
      maxOffhandDamage.toVarField(floatL, Private, Owner, SpecialInfo),
      bytes1.toVarField(fixedSizeT[Byte, _4](byte), Public),
      petNumber.toVarField(uint32L, Public),
      petNameTimestamp.toVarField(uint32L, Public),
      petExperience.toVarField(uint32L, Owner),
      petNextLevelExperience.toVarField(uint32L, Owner),
      dynamicFlags.toVarField(int32L, Dynamic),
      modCastSped.toVarField(floatL, Public),
      createdBySpell.toVarField(uint32L, Public),
      npcFlags.toVarField(int32L, Dynamic),
      emoteState.toVarField(uint32L, Public),
      stats.toVarField(fixedSizeT[Long, _5](uint32L), Public),
      positiveStats.toVarField(fixedSizeT[Long, _5](uint32L), Public),
      negativeStats.toVarField(fixedSizeT[Long, _5](uint32L), Public),
      resistances.toVarField(fixedSizeT[Long, _7](uint32L), Public),
      resistanceBuffModifierPositive.toVarField(fixedSizeT[Long, _7](uint32L), Public),
      resistanceBuffModifierNegative.toVarField(fixedSizeT[Long, _7](uint32L), Public),
      baseMana.toVarField(uint32L, Public),
      baseHealth.toVarField(uint32L, Public),
      bytes2.toVarField(fixedSizeT[Byte, _4](byte), Public),
      attackPower.toVarField(uint32L, Public),
      attackPowerMods.toVarField(uint16L ~ uint16L, Public),
      attackPowerMultiplier.toVarField(floatL, Public),
      minRangedDamage.toVarField(floatL, Public),
      maxRangedDamage.toVarField(floatL, Public),
      powerCostModifiers.toVarField(fixedSizeT[Float, _7](floatL), Public),
      powerCostMultiplier.toVarField(fixedSizeT[Float, _7](floatL), Public),
      maxHealthModifier.toVarField(floatL, Public),
      hoverHeight.toVarField(floatL, Public),
      new PaddingField
    )
  }
}