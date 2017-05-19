package wow.auth.protocol.packets

import scodec._
import scodec.codecs._
import wow.auth.protocol._
import wow.common.codecs._

import scala.collection.immutable

/**
  * Data structure which describes a realm
  *
  * @param realmType       the realm's type
  * @param lock            1 if it is locked otherwise 0
  * @param flags           the flags associated to the realm
  * @param name            the realm's name
  * @param address         the ip address of the realm
  * @param populationLevel the population level represented by a float
  * @param characterCount  number of characters on the realm
  * @param timeZone        the time zone
  * @param id              the identifier
  */
case class ServerRealmlistEntry(
  realmType: RealmTypes.Value,
  lock: Boolean,
  flags: RealmFlags.ValueSet,
  name: String,
  address: String,
  populationLevel: Float,
  characterCount: Int,
  timeZone: RealmTimeZones.Value,
  id: Int) {
}

object ServerRealmlistEntry {
  implicit val codec: Codec[ServerRealmlistEntry] = {
    ("realmType" | enumerated(uint8L, RealmTypes)) ::
      ("lock" | bool(8)) ::
      ("flag" | fixedBitmask(uint8L, RealmFlags)) ::
      ("name" | cstring) ::
      ("address" | cstring) ::
      ("populationLevel" | floatL) ::
      ("characterCount" | uint8L) ::
      ("timezone" | enumerated(uint8L, RealmTimeZones)) ::
      ("id" | uint8L)
  }.as[ServerRealmlistEntry]
}

/**
  * Data structure which describes the response to be sent
  *
  * @param realms a vector containing the realms
  */
case class ServerRealmlist(realms: immutable.Seq[ServerRealmlistEntry]) extends ServerPacket

object ServerRealmlist {
  implicit val codec: Codec[ServerRealmlist] = {
    constantE(OpCodes.RealmList) ::
      variableSizeBytes(
        uint16L,
        constantE(0L)(uint32L) ::
          ("realms" | sizePrefixedSeq(uint16L, Codec[ServerRealmlistEntry])) ::
          constantE(0x10)(uint8L) ::
          constantE(0x00)(uint8L))
  }.as[ServerRealmlist]
}

