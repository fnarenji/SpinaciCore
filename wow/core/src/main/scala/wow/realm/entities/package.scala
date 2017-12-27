package wow.realm

import scodec.Codec
import scodec.codecs._
import wow.common.database.DatabaseSerializableEnumeration

/**
  * Created by sknz on 4/28/17.
  */
package object entities {
  /**
    * Get character DAO
    * @param realm current realm
    * @return character DAO
    */
  def CharacterDao(implicit realm: RealmContextData): CharacterInfoDao = realm.characterDao

  object Genders extends Enumeration with DatabaseSerializableEnumeration {
    implicit lazy val codec: Codec[Value] = enumerated(uint8L, this)

    val Male = Value(0)
    val Female = Value(1)
    val None = Value(2)
  }

  object Races extends Enumeration with DatabaseSerializableEnumeration {
    implicit lazy val codec: Codec[Value] = enumerated(uint8L, this)

    val Human = Value(1)
    val Orc = Value(2)
    val Dwarf = Value(3)
    val NightElf = Value(4)
    val Undead = Value(5)
    val Tauren = Value(6)
    val Gnome = Value(7)
    val Troll = Value(8)
    val BloodElf = Value(10)
    val Draenei = Value(11)
  }

  object Classes extends Enumeration with DatabaseSerializableEnumeration {
    implicit lazy val codec: Codec[Value] = enumerated(uint8L, this)

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
