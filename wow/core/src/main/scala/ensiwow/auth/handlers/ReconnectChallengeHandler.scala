package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.Srp6Protocol
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets._
import ensiwow.auth.session._

import scala.util.Random

case class ReconnectChallenge(packet: ClientChallenge)

/**
  * Handles reconnect logon challenges
  */
class ReconnectChallengeHandler extends Actor with ActorLogging {
  val srp6 = new Srp6Protocol

  override def receive: PartialFunction[Any, Unit] = {
    case ReconnectChallenge(packet) =>
      val error = ChallengeHelper.validate(packet)

      val event = error match {
        case Some(authResult) =>
          val challenge = ServerReconnectChallenge(authResult, None)

          log.debug(s"Reconnect challenge failure response: $challenge")

          EventReconnectChallengeFailure(challenge)
        case None =>
          val userName = packet.login

          val RandomBitCount = 16 * 8
          val random = BigInt(RandomBitCount, Random)
          assert(random > 0)

          // Results
          val challengeData = ReconnectChallengeData(userName, random)

          val success = ServerReconnectChallengeSuccess(random)
          val response = ServerReconnectChallenge(AuthResults.Success, Some(success))

          log.debug(s"Reconnect challenge success response: $response")
          log.debug(s"Reconnect challenge values: $challengeData")

          EventReconnectChallengeSuccess(response, challengeData)
      }

      sender ! event
  }
}

object ReconnectChallengeHandler {
  val PreferredName = "ReconnectChallengeHandler"

  def props: Props = Props(classOf[ReconnectChallengeHandler])
}
