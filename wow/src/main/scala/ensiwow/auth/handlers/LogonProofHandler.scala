package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.Srp6Protocol
import ensiwow.auth.data.Account
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets.{ClientLogonProof, ServerLogonProof, ServerLogonProofFailure, ServerLogonProofSuccess}
import ensiwow.auth.session.{ChallengeData, EventProofFailure, EventProofSuccess, ProofData}

case class LogonProof(packet: ClientLogonProof, challengeData: ChallengeData)

/**
  * Handles logon proofs
  */
class LogonProofHandler extends Actor with ActorLogging {
  private val srp6 = new Srp6Protocol

  override def receive = {
    case LogonProof(packet, data@ChallengeData(login, srp6Identity, srp6Challenge)) =>

      val event = srp6.verify(login, packet.clientKey, packet.clientProof, srp6Identity, srp6Challenge) match {
        case Some(srp6Validation) =>
          Account.saveSessionKey(login, srp6Validation.sharedKey)

          val response = ServerLogonProof(
            AuthResults.Success,
            Some(ServerLogonProofSuccess(srp6Validation.serverProof)), None)

          EventProofSuccess(response, ProofData(data, srp6Validation.sharedKey))
        case _ =>
          val response = ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure()))

          EventProofFailure(response)
      }

      sender ! event
  }
}

object LogonProofHandler {
  val PreferredName = "LogonProofHandler"

  def props: Props = Props(classOf[LogonProofHandler])
}
