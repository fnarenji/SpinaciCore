package wow.auth.handlers

import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets.{ClientLogonProof, ServerLogonProof, ServerLogonProofFailure, ServerLogonProofSuccess}
import wow.auth.session.AuthSession.EventIncoming
import wow.auth.session._
import wow.auth.utils.PacketSerializer

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
          Account.saveSessionKey(login, srp6Validation.sharedKey)

          sendPacket(ServerLogonProof(AuthResults.Success,
            Some(ServerLogonProofSuccess(srp6Validation.serverProof)),
            None))

          goto(StateRealmlist) using NoData
        case None =>
          sendPacket(ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure())))
          goto(StateFailed) using NoData
      }
  }
}

