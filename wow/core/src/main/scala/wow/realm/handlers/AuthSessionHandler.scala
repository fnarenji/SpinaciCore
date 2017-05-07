package wow.realm.handlers

import wow.auth.data.Account
import wow.realm.protocol.payloads.{ClientAuthSession, ServerAuthResponse, ServerAuthResponseSuccess}
import wow.realm.protocol._
import wow.realm.session._

case class AuthSession(packet: ClientAuthSession)

/**
  * Handles realm auth session packet
  */
class AuthSessionHandler extends PayloadHandler[ClientAuthSession] {
  override def process(payload: ClientAuthSession): Unit = {
    val login = payload.login

    Account.findByLogin(login) match {
      case Some(Account(_, _, _, Some(sessionKey))) =>
        val response = ServerAuthResponse(AuthResponses.Ok, Some(ServerAuthResponseSuccess(None)))

        sender ! NetworkWorker.EventAuthenticated(sessionKey)
        sender ! NetworkWorker.EventOutgoing(response)
      case _ =>
        val response = ServerAuthResponse(AuthResponses.Failed, None)

        sender ! NetworkWorker.EventTerminateWithPayload(response)
    }
  }
}

object AuthSessionHandler extends PayloadHandlerFactory[AuthSessionHandler, ClientAuthSession]
