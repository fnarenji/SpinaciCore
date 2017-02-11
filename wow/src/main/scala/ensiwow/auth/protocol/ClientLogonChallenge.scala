package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Created by sknz on 2/7/17. */
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
                                login: String) {
  object WotlkVersionInfo {
    final val Major = 3
    final val Minor = 3
    final val Patch = 5
    final val Build = 12340
  }

  require(versionMajor == WotlkVersionInfo.Major)
  require(versionMinor == WotlkVersionInfo.Minor)
  require(versionPatch == WotlkVersionInfo.Patch)
  require(build == WotlkVersionInfo.Build)

  require(ip.length == 4)

  require(!login.isEmpty)
}

object ClientLogonChallenge {
  implicit val codec: Codec[ClientLogonChallenge] = {
    val reversedFixedCString = reverse(fixedCString)

    val GameName = "WoW"
    val PlatformLength = 4
    val OSLength = 4
    val CountryLength = 4

    constantE(OpCodes.LogonChallenge) ::
      ("error" | uint8L) ::
      ("size" | int16L) ::
      constantE(GameName)(reversedFixedCString) ::
      ("versionMajor" | uint8L) ::
      ("versionMinor" | uint8L) ::
      ("versionPatch" | uint8L) ::
      ("build" | uint16L) ::
      ("platform" | fixedSizeBytes(PlatformLength, reversedFixedCString)) ::
      ("os" | fixedSizeBytes(OSLength, reversedFixedCString)) ::
      ("country" | fixedSizeBytes(CountryLength, reversedFixedCString)) ::
      ("timezoneBias" | uint32L) ::
      ("ip" | vectorOfN(provide(4), uint8L)) ::
      ("login" | variableSizeBytes(uint8, "login" | ascii))
  }.as[ClientLogonChallenge]
}
