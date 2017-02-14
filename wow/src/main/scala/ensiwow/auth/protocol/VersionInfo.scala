package ensiwow.auth.protocol

import scodec.Codec
import scodec.codecs._

/**
  * Supported version info
  */
case class VersionInfo(Major: Int, Minor: Int, Patch: Int, Build: Int)

object VersionInfo {
  val SupportedVersionInfo = VersionInfo(3, 3, 5, 12340)

  implicit val codec: Codec[VersionInfo] = {
    ("versionMajor" | uint8L) ::
      ("versionMinor" | uint8L) ::
      ("versionPatch" | uint8L) ::
      ("build" | uint16L)
  }.as[VersionInfo]
}
