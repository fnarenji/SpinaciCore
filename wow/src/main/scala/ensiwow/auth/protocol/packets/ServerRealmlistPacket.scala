package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{OpCodes, ServerPacket}
import scodec._
import scodec.codecs._
import scala.language.postfixOps

/**
  * Data structure which describes a realm
  * @param realmType the realm's type
  * @param lock 1 if it is locked otherwise 0
  * @param flags the flags associated to the realm
  * @param name the realm's name
  * @param ip the ip address to which the response will be sent
  * @param populationLevel the population level represented by a float
  * @param characterCount number of characters on the realm
  * @param timezone the time zone
  * @param id the identifier
  */
case class ServerRealmlistPacketEntry(realmType: Int,
                           lock: Int,
                           flags: Int,
                           name: String,
                           ip: String,
                           populationLevel: Float,
                           characterCount: Int,
                           timezone: Int,
                          id: Int) {

  /** Size of the encoded packet. In addition of the strings, the packet will stores 10 bytes of data */
  def packetSize = name.length + ip.length + 10
}

/**
  * An intermediary class which stores enough data to generate a response of type ServerRealmlistPacket
  * @param realms A vector containing all the realms to be sent in the response
  */
case class ServerRealmlistPacketBody(realms: Vector[ServerRealmlistPacketEntry]) {

  /** Size of the encoded packet. In addition of the realms, it stores 10 bytes of data */
  def packetSize = (for {realm <- realms} yield realm.packetSize).sum  + 10

  /** Generate the response */
  def computePacket = ServerRealmlistPacket(packetSize, realms.size, realms)
}

/**
  * Data structure which describes the response to be sent
  * @param packetSize the size of the packet
  * @param realmsCount the number of realms
  * @param realms a vector containing the realms
  */
case class ServerRealmlistPacket(packetSize: Int,
                                 realmsCount: Int,
                                 realms: Vector[ServerRealmlistPacketEntry]) extends ServerPacket {
  require(realmsCount == realms.size)
}

object ServerRealmlistPacketEntry {
  implicit val codec: Codec[ServerRealmlistPacketEntry] = {
    ("realmType" | uint8L) ::
      ("lock" | uint8L) ::
      ("flag" | uint8L) ::
      ("name" | cstring) ::
      ("ip" | cstring) ::
      ("populationLevel" | floatL) ::
      ("characterCount" | uint8L) ::
      ("timezone" | uint8L) ::
      ("id" | uint8L)
  }.as[ServerRealmlistPacketEntry]
}

object ServerRealmlistPacket {
  implicit val codec: Codec[ServerRealmlistPacket] = {
    constantE(OpCodes.RealmList) ::
      ("packetSize" | uint16L) ::
      constantE(0L)(uint32L) ::
      (("realmsCount" | uint16L) >>:~ { realmsCount =>
        ("realms" | vectorOfN(provide(realmsCount), Codec[ServerRealmlistPacketEntry])) ::
          constantE(0x10)(uint8L) ::
          constantE(0x00)(uint8L)
      })
  }.as[ServerRealmlistPacket]
}

