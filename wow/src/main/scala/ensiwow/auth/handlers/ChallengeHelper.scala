package ensiwow.auth.handlers

import ensiwow.auth.protocol.{AuthResults, VersionInfo}
import ensiwow.auth.protocol.packets.ClientChallenge

/**
  * Created by sknz on 2/19/17.
  */
object ChallengeHelper {
  def validate(packet: ClientChallenge) = validateVersion(packet)

  private def validateVersion(packet: ClientChallenge) = {
    val valid = packet.versionInfo == VersionInfo.SupportedVersionInfo

    if (!valid) {
      Some(AuthResults.FailVersionInvalid)
    } else {
      None
    }
  }
}
