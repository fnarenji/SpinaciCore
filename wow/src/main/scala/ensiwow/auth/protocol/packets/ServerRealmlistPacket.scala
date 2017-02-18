package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{OpCodes, VersionInfo}
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Created by yanncolina on 15/02/17.
  */

case class ServerRealmPacket(realmType: Int,
                           lock: Int,
                           flags: Int,
                           name: String,
                           ip: String,
                           populationLevel: Float,
                           characterCount: Int,
                           timezone: Int,
                           id: Int,
                           versionInfo: VersionInfo) {
  require(versionInfo == VersionInfo.SupportedVersionInfo)
}

case class ServerRealmlistPacket(packetSize: Int,
                                 nbrRealms: Int,
                                 realms: Vector[ServerRealmPacket]) extends ServerPacket {
  require(nbrRealms == realms.size)
}

object ServerRealmPacket {
  implicit val codec: Codec[ServerRealmPacket] = {
    ("realmType" | uint8L) ::
      ("lock" | uint8L) ::
      ("flag" | uint8L) ::
      ("name" | reverse(cstring)) ::
      ("ip" | reverse(cstring)) ::
      ("populationLevel" | floatL) ::
      ("characterCount" | uint8L) ::
      ("timezone" | uint8L) ::
      ("id" | uint8L) ::
      ("versionInfo" | Codec[VersionInfo])
  }.as[ServerRealmPacket]
}

object ServerRealmlistPacket {
  implicit val codec: Codec[ServerRealmlistPacket] = {
    constantE(OpCodes.RealmList) ::
      ("packetSize" | uint8L) ::
      constantE(0L)(uint32L) ::
      (("nbrRealms" | uint16L) >>:~ { nbrRealms =>
        ("realms" | vectorOfN(provide(nbrRealms), Codec[ServerRealmPacket])) ::
          constantE(0x10)(uint8L) ::
          constantE(0x00)(uint8L)
      })
  }.as[ServerRealmlistPacket]
}

