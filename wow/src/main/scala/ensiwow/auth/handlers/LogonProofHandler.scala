package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.protocol.AuthResults
import ensiwow.auth.protocol.packets.{ServerLogonProof, ServerLogonProofFailure}
import ensiwow.auth.session.EventLogonFailure

case class LogonProof()

/**
  * Handles logon proofs
  */
class LogonProofHandler extends Actor with ActorLogging {
  override def receive: PartialFunction[Any, Unit] = {
    case LogonProof() =>
      val logonProof = ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure()))

      sender ! EventLogonFailure(logonProof)
  }
}

object LogonProofHandler {
  val PreferredName = "LogonProofHandler"

  def props: Props = Props(classOf[LogonProofHandler])
}
