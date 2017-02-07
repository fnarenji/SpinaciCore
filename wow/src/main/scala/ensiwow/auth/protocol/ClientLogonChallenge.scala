package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
case class ClientLogonChallenge(opCode: Int,
                                error: Int,
                                size: Int,
                                versionMajor: Int,
                                versionMinor: Int,
                                versionPatch: Int,
                                build: Int,
                                platform: String,
                                os: String,
                                country: String,
                                timezoneBias: Long,
                                ip: Long,
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

  require(!login.isEmpty)
}

object ClientLogonChallenge {
  val reversedAscii = reverse(ascii)
  val reversedFixedCString = reverse(fixedCString)
  final val GameName = reversedFixedCString encode "WoW" require

  final val PlatformLength = 4
  final val OSLength = 4
  final val CountryLength = 4

  implicit val codec: Codec[ClientLogonChallenge] = {
    ("opCode" | uint8L) ::
      ("error" | uint8L) ::
      ("size" | int16L) ::
      constant(GameName) ::
      ("versionMajor" | uint8L) ::
      ("versionMinor" | uint8L) ::
      ("versionPatch" | uint8L) ::
      ("build" | uint16L) ::
      ("platform" | fixedSizeBytes(PlatformLength, reversedFixedCString)) ::
      ("os" | fixedSizeBytes(OSLength, reversedFixedCString)) ::
      ("country" | fixedSizeBytes(CountryLength, reversedFixedCString)) ::
      ("timezoneBias" | uint32L) ::
      ("ip" | uint32L) ::
      ("login" | variableSizeBytes(uint8, "login" | ascii))
  }.as[ClientLogonChallenge]
}
