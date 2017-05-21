package wow.auth.handlers

import akka.pattern.ask
import akka.util.Timeout
import wow.auth.AccountsState
import wow.auth.AccountsState.IsOnline
import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets.{ClientLogonProof, ServerLogonProof, ServerLogonProofFailure, ServerLogonProofSuccess}
import wow.auth.session.AuthSession.EventIncoming
import wow.auth.session._
import wow.auth.utils.PacketSerializer

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Handles logon proofs
  */
trait LogonProofHandler {
  this: AuthSession =>

  def handleProof: StateFunction = {
    case Event(EventIncoming(bits), ChallengeData(login, srp6Identity, srp6Challenge)) =>
      def fail(reason: AuthResults.AuthResult) = {
        sendPacket(ServerLogonProof(reason, None, Some(ServerLogonProofFailure())))

        goto(StateFailed) using NoData
      }

      log.debug("Received proof")
      val packet = PacketSerializer.deserialize[ClientLogonProof](bits)
      log.debug(packet.toString)

      srp6.verify(login, packet.clientKey, packet.clientProof, srp6Identity, srp6Challenge) match {
        case Some(srp6Validation) =>
          val accountState = context.actorSelection(AccountsState.ActorPath)

          implicit val timeout = Timeout(5 seconds)
          val askIsOnline = (accountState ? IsOnline(login)).mapTo[Boolean]
          val isOnline = Await.result(askIsOnline, timeout.duration)

          if (isOnline) {
            fail(AuthResults.FailAlreadyOnline)
          } else {
           Account.findByLogin(login) match {
             case Some(account) =>
               Account.save(account.copy(sessionKey = Some(srp6Validation.sharedKey)))

               sendPacket(ServerLogonProof(AuthResults.Success,
                 Some(ServerLogonProofSuccess(srp6Validation.serverProof)),
                 None))

               goto(StateRealmlist) using RealmsListData(account)

             case None =>
               fail(AuthResults.FailUnknownAccount)
           }
          }
        case None =>
          fail(AuthResults.FailUnknownAccount)
      }
  }
}

