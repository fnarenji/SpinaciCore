package ensiwow.auth.handlers

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.{Srp6Challenge, Srp6Constants, Srp6Identity, Srp6Protocol}
import ensiwow.auth.protocol.packets.{ClientLogonProof, ServerLogonProof, ServerLogonProofFailure, ServerLogonProofSuccess}
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.session.{ChallengeData, EventLogonFailure, EventLogonSuccess}
import ensiwow.utils.BigIntExtensions._
import scodec.bits.ByteVector

case class LogonProof(packet: ClientLogonProof, challengeData: ChallengeData)

/**
  * Handles logon proofs
  */
class LogonProofHandler extends Actor with ActorLogging {
  private val srp6 = new Srp6Protocol

  override def receive = {
    case LogonProof(packet, ChallengeData(login, srp6Identity, srp6Challenge)) =>

      val event = srp6.verify(login, packet.clientKey, packet.clientProof, srp6Identity, srp6Challenge) match {
        case Some(srp6Validation) =>
          val response = ServerLogonProof(AuthResults.Success, Some(ServerLogonProofSuccess(srp6Validation.serverProof)), None)

          EventLogonSuccess(response)
        case _ =>
          val response = ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure()))

          EventLogonFailure(response)
      }

      sender ! event
  }
}

object LogonProofHandler {
  val PreferredName = "LogonProofHandler"

  def props: Props = Props(classOf[LogonProofHandler])
}
