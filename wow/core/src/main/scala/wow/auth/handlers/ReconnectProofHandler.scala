package wow.auth.handlers

import akka.actor.FSM
import wow.auth.data.Account
import wow.auth.protocol.AuthResults
import wow.auth.protocol.packets._
import wow.auth.session.AuthSession.EventIncoming
import wow.auth.session._
import wow.auth.utils.PacketSerializer

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
          val state: FSM.State[AuthSessionState, AuthSessionData] = goto(StateRealmlist) using RealmsListData(login)
          (state, AuthResults.Success)
        case _ =>
          val state: FSM.State[AuthSessionState, AuthSessionData] = goto(StateFailed) using NoData
          (state, AuthResults.FailUnknownAccount)
      }

      sendPacket(ServerReconnectProof(authResult))
      nextState
  }
}

