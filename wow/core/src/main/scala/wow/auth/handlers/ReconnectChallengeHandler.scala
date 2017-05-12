package wow.auth.handlers

import wow.auth.protocol.AuthResults
import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.packets._
import wow.auth.session.AuthSession.EventIncoming
import wow.auth.session._
import wow.auth.utils.PacketSerializer
import wow.common.VersionInfo

import scala.util.Random

case class ReconnectChallenge(packet: ClientChallenge)

/**
  * Handles reconnect logon challenges
  */
trait ReconnectChallengeHandler {
  this: AuthSession =>

  def handleReconnectChallenge: StateFunction = {
    case Event(EventIncoming(bits), NoData) =>
      log.debug("Received reconnect challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.reconnectChallengeCodec)
      log.debug(packet.toString)

      def fail(authResult: AuthResult) = {
        sendPacket(ServerReconnectChallenge(authResult, None))
        goto(StateFailed) using NoData
      }

      if (packet.versionInfo != VersionInfo.SupportedVersionInfo) {
        fail(AuthResults.FailVersionInvalid)
      } else {
        val login = packet.login

        val RandomBitCount = 16 * 8
        val random = BigInt(RandomBitCount, Random)
        assert(random > 0)

        sendPacket(ServerReconnectChallenge(AuthResults.Success, Some(ServerReconnectChallengeSuccess(random))))
        goto(StateReconnectProof) using ReconnectChallengeData(login, random)
      }
  }
}

