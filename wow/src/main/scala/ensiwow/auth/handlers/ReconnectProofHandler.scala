package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.Srp6Protocol
import ensiwow.auth.data.Account
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets._
import ensiwow.auth.session._

case class ReconnectProof(packet: ClientReconnectProof, reconnectChallengeData: ReconnectChallengeData)

/**
  * Handles reconnect logon proofs
  */
class ReconnectProofHandler extends Actor with ActorLogging {
  private val srp6 = new Srp6Protocol

  override def receive: PartialFunction[Any, Unit] = {
    case ReconnectProof(packet, ReconnectChallengeData(login, random)) =>
      val sharedKey = Account.getSessionKey(login)

      val verified = srp6.reverify(login, random, packet.clientKey, packet.clientProof, sharedKey)

      val event = if (verified) {
        val response = ServerReconnectProof(AuthResults.Success)

        EventReconnectProofSuccess(response)
      } else {
        EventReconnectProofFailure
      }

      sender ! event
  }
}

object ReconnectProofHandler {
  val PreferredName = "ReconnectProofHandler"

  def props: Props = Props(classOf[ReconnectProofHandler])
}