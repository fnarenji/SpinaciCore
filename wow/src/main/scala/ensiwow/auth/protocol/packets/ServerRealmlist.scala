package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.{OpCodes, ServerPacket}
import ensiwow.common.codecs._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/** * Data structure which describes a realm
  *
  * @param realmType       the realm's type
  * @param lock            1 if it is locked otherwise 0
  * @param flags           the flags associated to the realm
  * @param name            the realm's name
  * @param ip              the ip address to which the response will be sent
  * @param populationLevel the population level represented by a float
  * @param characterCount  number of characters on the realm
  * @param timezone        the time zone
  * @param id              the identifier
  */
case class ServerRealmlistEntry(realmType: Int,
                                lock: Int,
                                flags: Int,
                                name: String,
                                ip: String,
                                populationLevel: Float,
                                characterCount: Int,
                                timezone: Int,
                                id: Int)

/**
  * Data structure which describes the response to be sent
  *
  * @param realms a vector containing the realms
  */
case class ServerRealmlist(realms: Vector[ServerRealmlistEntry]) extends ServerPacket

object ServerRealmlistEntry {
  implicit val codec: Codec[ServerRealmlistEntry] = {
    ("realmType" | uint8L) ::
      ("lock" | uint8L) ::
      ("flag" | uint8L) ::
      ("name" | cstring) ::
      ("ip" | cstring) ::
      ("populationLevel" | floatL) ::
      ("characterCount" | uint8L) ::
      ("timezone" | uint8L) ::
      ("id" | uint8L)
  }.as[ServerRealmlistEntry]
}

object ServerRealmlist {
  implicit val codec: Codec[ServerRealmlist] = {
    constantE(OpCodes.RealmList) ::
      variableSizeBytes(
        uint16L,
        constantE(0L)(uint32L) ::
          ("realms" | variableSizeVector(uint16L, Codec[ServerRealmlistEntry])) ::
          constantE(0x10)(uint8L) ::
          constantE(0x00)(uint8L))
  }.as[ServerRealmlist]
}

