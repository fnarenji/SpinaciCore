package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.{OpCodes, WotlkVersionInfo}
import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Client logon challenge packet. First packet sent by client.
  **/
case class ClientLogonChallenge(error: Int,
                                size: Int,
                                versionMajor: Int,
                                versionMinor: Int,
                                versionPatch: Int,
                                build: Int,
                                platform: String,
                                os: String,
                                country: String,
                                timezoneBias: Long,
                                ip: Vector[Int],
                                login: String) extends ClientPacket {
  require(ip.length == 4)
  require(!login.isEmpty)
}

object ClientLogonChallenge {
  implicit val codec: Codec[ClientLogonChallenge] = {
    def reversedFixedSizeCString(sizeInBytes: Long) = reverse(fixedCString(sizeInBytes))

    val GameName = "WoW"
    val GameNameLength = 4
    val PlatformLength = 4
    val OSLength = 4
    val CountryLength = 4

    constantE(OpCodes.LogonChallenge) ::
      ("error" | uint8L) ::
      ("size" | int16L) ::
      constantE(GameName)(reversedFixedSizeCString(GameNameLength)) ::
      ("versionMajor" | uint8L) ::
      ("versionMinor" | uint8L) ::
      ("versionPatch" | uint8L) ::
      ("build" | uint16L) ::
      ("platform" | reversedFixedSizeCString(PlatformLength)) ::
      ("os" | reversedFixedSizeCString(OSLength)) ::
      ("country" | reversedFixedSizeCString(CountryLength)) ::
      ("timezoneBias" | uint32L) ::
      ("ip" | vectorOfN(provide(4), uint8L)) ::
      ("login" | variableSizeBytes(uint8L, "login" | ascii))
  }.as[ClientLogonChallenge]
}
