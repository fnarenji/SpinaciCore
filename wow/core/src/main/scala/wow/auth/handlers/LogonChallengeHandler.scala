package wow.auth.handlers

import wow.auth.crypto.Srp6Constants
import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.packets.{ClientChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import wow.auth.session.AuthSession.EventIncoming
import wow.auth.session._
import wow.auth.utils.PacketSerializer
import wow.common.VersionInfo

import scala.util.Random

/**
  * Handles logon challenges
  */
trait LogonChallengeHandler {
  this: AuthSession =>

  def handleChallenge: StateFunction = {
    case Event(EventIncoming(bits), NoData) =>
      log.debug("Received challenge")
      val packet = PacketSerializer.deserialize[ClientChallenge](bits)(ClientChallenge.logonChallengeCodec)
      log.debug(packet.toString)

      def fail(authResult: AuthResult) = {
        val challenge = ServerLogonChallenge(authResult, None)

        sendPacket(challenge)
        goto(StateFailed) using NoData
      }

      // Version check
      if (packet.versionInfo != VersionInfo.SupportedVersionInfo) {
        fail(AuthResults.FailVersionInvalid)
      } else {
        val login = packet.login
        Account.findByLogin(login) match {
          case Some(Account(_, _, identity, _)) =>
            val srp6Challenge = srp6.computeChallenge(identity)

            // Results
            val challengeData = ChallengeData(packet.login, identity, srp6Challenge)

            val Unk3BitCount = 16 * 8
            val unk3 = BigInt(Unk3BitCount, Random)
            assert(unk3 > 0)

            val success = ServerLogonChallengeSuccess(srp6Challenge.serverKey,
              Srp6Constants.g.toInt,
              Srp6Constants.N,
              identity.salt,
              unk3)

            log.debug(s"Challenge values: $challengeData")

            sendPacket(ServerLogonChallenge(AuthResults.Success, Some(success)))
            goto(StateProof) using challengeData
          case _ =>
            fail(AuthResults.FailUnknownAccount)
        }
      }
  }
}

