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
            sendPacket(ServerLogonProof(AuthResults.FailAlreadyOnline, None, Some(ServerLogonProofFailure())))

            goto(StateFailed) using NoData
          } else {
            Account.saveSessionKey(login, srp6Validation.sharedKey)

            sendPacket(ServerLogonProof(AuthResults.Success,
              Some(ServerLogonProofSuccess(srp6Validation.serverProof)),
              None))

            goto(StateRealmlist) using NoData
          }
        case None =>
          sendPacket(ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure())))
          goto(StateFailed) using NoData
      }
  }
}

