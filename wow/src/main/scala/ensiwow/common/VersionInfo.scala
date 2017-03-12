package ensiwow.common

import scodec.Codec
import scodec.codecs._

/**
  * Game expansions
  */
object Expansions extends Enumeration {
  val Vanilla = Value(0)
  val TheBurningCrusade = Value(1)
  val WrathOfTheLichKing = Value(2)
}

/**
  * Supported version info
  */
case class VersionInfo(Major: Int, Minor: Int, Patch: Int, Build: Int)

object VersionInfo {
  /**
    * Version for which this server accepts connections
    */
  val SupportedVersionInfo = VersionInfo(3, 3, 5, 12340)
  val SupportedExpansion = Expansions.WrathOfTheLichKing

  implicit val codec: Codec[VersionInfo] = {
    ("versionMajor" | uint8L) ::
      ("versionMinor" | uint8L) ::
      ("versionPatch" | uint8L) ::
      ("build" | uint16L)
  }.as[VersionInfo]
}
