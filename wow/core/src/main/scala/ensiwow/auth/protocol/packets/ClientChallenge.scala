package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.{ClientPacket, OpCodes}
import ensiwow.common.VersionInfo
import ensiwow.common.codecs._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Client logon challenge packet. First packet sent by client.
  **/
case class ClientChallenge(error: Int,
                           size: Int,
                           versionInfo: VersionInfo,
                           platform: String,
                           os: String,
                           country: String,
                           timezoneBias: Long,
                           ip: Vector[Int],
                           login: String) extends ClientPacket {
  require(ip.length == 4)
  require(!login.isEmpty)
}

object ClientChallenge {
  private def makeChallengeCodec(opCode: OpCodes.Value) = {
    def reversedFixedSizeCString(sizeInBytes: Long) = reverse(fixedCString(sizeInBytes))

    val GameName = "WoW"
    val GameNameLength = 4
    val PlatformLength = 4
    val OSLength = 4
    val CountryLength = 4

    {
      constantE(opCode) ::
        ("error" | uint8L) ::
        ("size" | int16L) ::
        constantE(GameName)(reversedFixedSizeCString(GameNameLength)) ::
        ("versionInfo" | Codec[VersionInfo]) ::
        ("platform" | reversedFixedSizeCString(PlatformLength)) ::
        ("os" | reversedFixedSizeCString(OSLength)) ::
        ("country" | reversedFixedSizeCString(CountryLength)) ::
        ("timezoneBias" | uint32L) ::
        ("ip" | vectorOfN(provide(4), uint8L)) ::
        ("login" | variableSizeBytes(uint8L, "login" | ascii))
    }.as[ClientChallenge]
  }

  val logonChallengeCodec: Codec[ClientChallenge] = makeChallengeCodec(OpCodes.LogonChallenge)
  val reconnectChallengeCodec: Codec[ClientChallenge] = makeChallengeCodec(OpCodes.ReconnectChallenge)
}
