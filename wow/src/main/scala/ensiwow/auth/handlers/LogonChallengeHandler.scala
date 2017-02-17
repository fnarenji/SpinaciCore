package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.{Srp6Constants, Srp6Protocol}
import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import ensiwow.auth.protocol.{AuthResults, VersionInfo}
import ensiwow.auth.session.{ChallengeData, EventChallengeFailure, EventChallengeSuccess}

import scala.util.Random

case class LogonChallenge(packet: ClientLogonChallenge)

/**
  * Handles logon challenges
  */
class LogonChallengeHandler extends Actor with ActorLogging {
  val srp6 = new Srp6Protocol

  override def receive: PartialFunction[Any, Unit] = {
    case LogonChallenge(packet) =>
      val error = validateVersion(packet)

      val event = error match {
        case Some(authResult) =>
          val challenge = ServerLogonChallenge(authResult, None)

          log.debug(s"Challenge failure response: $challenge")

          EventChallengeFailure(challenge)
        case None =>
          val userName = packet.login

          // TODO: non hardcoded password
          val password = "t"

          // TODO: this should be computed a single time upon account creation
          val srp6Identity = srp6.computeSaltAndVerifier(userName, password)

          val srp6Challenge = srp6.computeChallenge(srp6Identity)

          // Results
          val challengeData = ChallengeData(packet.login, srp6Identity, srp6Challenge)

          val Unk3BitCount = 16 * 8
          val unk3 = BigInt(Unk3BitCount, Random)
          assert(unk3 > 0)

          val success = ServerLogonChallengeSuccess(srp6Challenge.serverKey, Srp6Constants.g.toInt, Srp6Constants.N,
            srp6Identity.salt, unk3)
          val response = ServerLogonChallenge(AuthResults.Success, Some(success))

          log.debug(s"Challenge success response: $response")
          log.debug(s"Challenge values: $challengeData")

          EventChallengeSuccess(response, challengeData)
      }

      sender ! event
  }

  private def validateVersion(packet: ClientLogonChallenge): Option[AuthResult] = {
    val valid = packet.versionInfo == VersionInfo.SupportedVersionInfo

    if (!valid) {
      Some(AuthResults.FailVersionInvalid)
    } else {
      None
    }
  }
}

object LogonChallengeHandler {
  val PreferredName = "LogonChallengeHandler"

  def props: Props = Props(classOf[LogonChallengeHandler])
}
