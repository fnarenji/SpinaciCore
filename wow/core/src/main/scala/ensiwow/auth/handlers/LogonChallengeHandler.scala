package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.{Srp6Constants, Srp6Protocol}
import ensiwow.auth.data.Account
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets.{ClientChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import ensiwow.auth.session.{ChallengeData, EventChallengeFailure, EventChallengeSuccess}
import ensiwow.common.VersionInfo

import scala.util.Random

case class LogonChallenge(packet: ClientChallenge)

/**
  * Handles logon challenges
  */
class LogonChallengeHandler extends Actor with ActorLogging {
  val srp6 = new Srp6Protocol

  override def receive: PartialFunction[Any, Unit] = {
    case LogonChallenge(packet) =>
      def versionCheck = if (packet.versionInfo != VersionInfo.SupportedVersionInfo) {
        Some(AuthResults.FailVersionInvalid)
      } else {
        None
      }

      val login = packet.login
      def getAccount: Option[AuthResults.AuthResult] = Account.findByLogin(login) match {
        case Some(Account(_, _, identity, _)) =>
          val srp6Challenge = srp6.computeChallenge(identity)

          // Results
          val challengeData = ChallengeData(packet.login, identity, srp6Challenge)

          val Unk3BitCount = 16 * 8
          val unk3 = BigInt(Unk3BitCount, Random)
          assert(unk3 > 0)

          val success = ServerLogonChallengeSuccess(srp6Challenge.serverKey, Srp6Constants.g.toInt, Srp6Constants.N,
            identity.salt, unk3)
          val response = ServerLogonChallenge(AuthResults.Success, Some(success))

          log.debug(s"Challenge success response: $response")
          log.debug(s"Challenge values: $challengeData")

          sender ! EventChallengeSuccess(response, challengeData)

          None
        case _ =>
          Some(AuthResults.FailUnknownAccount)
      }

      val error = versionCheck.orElse(getAccount)

      error foreach { authResult =>
        val challenge = ServerLogonChallenge(authResult, None)

        log.debug(s"Challenge failure response: $challenge")

        sender ! EventChallengeFailure(challenge)
      }
  }
}

object LogonChallengeHandler {
  val PreferredName = "LogonChallengeHandler"

  def props: Props = Props(classOf[LogonChallengeHandler])
}
