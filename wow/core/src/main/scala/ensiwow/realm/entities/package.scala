package ensiwow.realm

import ensiwow.common.codecs.{EnumCodecProvider, NumericCodecTag}
import scodec.codecs._

/**
  * Created by sknz on 4/28/17.
  */
package object entities {
  object Genders extends Enumeration with EnumCodecProvider[Int] {
    override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

    val Male = Value(0)
    val Female = Value(1)
    val None = Value(2)
  }

  object Races extends Enumeration with EnumCodecProvider[Int] {
    override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

    val Human = Value(1)
    val Orc = Value(2)
    val Dwarf = Value(3)
    val Nightelf = Value(4)
    val UndeadPlayer = Value(5)
    val Tauren = Value(6)
    val Gnome = Value(7)
    val Troll = Value(8)
    val Bloodelf = Value(10)
    val Draenei = Value(11)
  }

  object Classes extends Enumeration with EnumCodecProvider[Int] {
    override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

    val Warrior = Value(1)
    val Paladin = Value(2)
    val Hunter = Value(3)
    val Rogue = Value(4)
    val Priest = Value(5)
    val DeathKnight = Value(6)
    val Shaman = Value(7)
    val Mage = Value(8)
    val Warlock = Value(9)
    val Druid = Value(11)
  }
}
