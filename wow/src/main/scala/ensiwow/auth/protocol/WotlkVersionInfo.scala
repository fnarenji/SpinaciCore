package ensiwow.auth.protocol

/**
  * Supported version info
  */
object WotlkVersionInfo {
  final val Major = 3
  final val Minor = 3
  final val Patch = 5
  final val Build = 12340

  override def toString = s"WotlkVersionInfo($Major.$Minor.$Patch $Build)"
}
