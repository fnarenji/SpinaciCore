package wow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import wow.auth.crypto.Srp6Protocol
import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets._
import wow.auth.session._

case class ReconnectProof(packet: ClientReconnectProof, reconnectChallengeData: ReconnectChallengeData)

/**
  * Handles reconnect logon proofs
  */
class ReconnectProofHandler extends Actor with ActorLogging {
  private val srp6 = new Srp6Protocol

  override def receive: PartialFunction[Any, Unit] = {
    case ReconnectProof(packet, ReconnectChallengeData(login, random)) =>
      var event: AuthSessionEvent = EventReconnectProofFailure

      Account.findByLogin(login) foreach {
        case Account(_, _, _, Some(sessionKey)) =>
          val verified = srp6.reverify(login, random, packet.clientKey, packet.clientProof, sessionKey)

          if (verified) {
            val response = ServerReconnectProof(AuthResults.Success)

            event = EventReconnectProofSuccess(response)
          }
      }

      sender ! event
  }
}

object ReconnectProofHandler {
  val PreferredName = "ReconnectProofHandler"

  def props: Props = Props(classOf[ReconnectProofHandler])
}
