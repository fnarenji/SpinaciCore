package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{OpCodes, ServerPacket, VersionInfo}
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Created by yanncolina on 15/02/17.
  */

case class ServerRealmlistPacketEntry(realmType: Int,
                           lock: Int,
                           flags: Int,
                           name: String,
                           ip: String,
                           populationLevel: Float,
                           characterCount: Int,
                           timezone: Int,
                           id: Int)

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

