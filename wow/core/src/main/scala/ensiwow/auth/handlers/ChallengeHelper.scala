package ensiwow.auth.handlers

import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets.ClientChallenge
import ensiwow.common.VersionInfo

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
