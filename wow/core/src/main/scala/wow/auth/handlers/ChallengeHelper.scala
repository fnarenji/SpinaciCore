package wow.auth.handlers

import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets.ClientChallenge
import wow.common.VersionInfo

/**
  * Created by sknz on 2/19/17.
  */
object ChallengeHelper {
  def validate(packet: ClientChallenge): Option[AuthResult] = validateVersion(packet)

  private def validateVersion(packet: ClientChallenge) = {
    val valid = packet.versionInfo == VersionInfo.SupportedVersionInfo

    if (!valid) {
      Some(AuthResults.FailVersionInvalid)
    } else {
      None
    }
  }
}
