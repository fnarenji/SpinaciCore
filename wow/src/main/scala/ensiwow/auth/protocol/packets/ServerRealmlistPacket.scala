package ensiwow.auth.protocol.packets

import scala.language.postfixOps
import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{OpCodes, VersionInfo}
import scodec._
import scodec.codecs._

/**
  * Created by yanncolina on 15/02/17.
  */

case class RealmlistPacket(realmType: Int,
                           lock: Int,
                           flags: Int,
                           name: String,
                           ip: String,
                           populationLevel: Float,
                           characterCount: Int,
                           timezone: Int,
                           id: Int,
                           versionInfo: VersionInfo)

case class ServerRealmlistPacket(packetSize: Int,
                                 voidInt: Int,
                                 nbrRealms: Int,
                                 realms: Vector[RealmlistPacket],
                                 fixedSlot1: Int,
                                 fixedSlot2: Int) extends ServerPacket

object RealmlistPacket {
  implicit val codec: Codec[RealmlistPacket] = {
    def reversedFixedSizeCString(sizeInBytes: Long) = reverse(fixedCString(sizeInBytes))
    ("realmType" | uint8L) ::
      ("lock" | uint8L) ::
      ("flag" | uint8L) ::
      ("name" | variableSizeBytes(uint8L, "name" | ascii))
      ("ip" | reversedFixedSizeCString(4)) ::
      ("populationLevel" | uint8L) ::
      ("characterCount" | uint8L) ::
      ("timezone" | uint8L) ::
      ("id" | uint8L) ::
      ("versionInfo" | Codec[VersionInfo])
  }.as[RealmlistPacket]
}

object ServerRealmlistPacket {
 implicit val codec: Codec[ServerRealmlistPacket] = {
   constantE(OpCodes.RealmList) ::
     ("packetSize" | uint8L) ::
     ("voidInt" | uint32L) ::
     (("nbrRealms" | uint8L) >>:~ { nbrRealms => ("realms" | vectorOfN(provide(nbrRealms), Codec[RealmlistPacket])).hlist })
     ("fixedSlot1" | uint8L) ::
     ("fixedSlot1" | uint8L)
 }.as[ServerRealmlistPacket]
}

