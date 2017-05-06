package wow.auth.handlers

import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets._
import wow.auth.session._
import wow.auth.utils.PacketSerializer
import wow.common.network.EventIncoming

/**
  * Handles reconnect logon proofs
  */
trait ReconnectProofHandler {
  this: AuthSession =>

  def handleReconnectProof: StateFunction = {
    case Event(EventIncoming(bits), ReconnectChallengeData(login, random)) =>
      log.debug("Received reconnect proof")
      val packet = PacketSerializer.deserialize[ClientReconnectProof](bits)
      log.debug(packet.toString)

      def reverify(sessionKey: BigInt) = srp6.reverify(login, random, packet.clientKey, packet.clientProof, sessionKey)

      val account = Account.findByLogin(login)
      val (nextState, authResult) = account match {
        case Some(Account(_, _, _, Some(sessionKey))) if reverify(sessionKey) =>
          (StateRealmlist, AuthResults.Success)
        case _ =>
          (StateFailed, AuthResults.FailUnknownAccount)
      }

      sendPacket(ServerReconnectProof(authResult))
      goto(nextState) using NoData
  }
}

