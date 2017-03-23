package ensiwow.realm.handlers

import ensiwow.auth.data.Account
import ensiwow.realm.protocol.payloads.{ClientAuthSession, ServerAuthResponse, ServerAuthResponseSuccess}
import ensiwow.realm.protocol._
import ensiwow.realm.session._

case class AuthSession(packet: ClientAuthSession)

/**
  * Handles realm auth session packet
  */
class AuthSessionHandler extends PayloadHandler[ClientAuthSession] {
  override def process(payload: ClientAuthSession): Unit = {
    val userName = payload.login

    Account.getSessionKey(userName) match {
      case Some(sessionKey) =>
        val response = ServerAuthResponse(AuthResponses.Ok, Some(ServerAuthResponseSuccess(None)))

        sender ! NetworkWorker.EventAuthenticated(sessionKey)
        sender ! NetworkWorker.EventOutgoing(response)
      case None =>
        val response = ServerAuthResponse(AuthResponses.Failed, None)

        sender ! NetworkWorker.EventTerminateWithPayload(response)
    }
  }
}

object AuthSessionHandler extends PayloadHandlerFactory[AuthSessionHandler, ClientAuthSession]
