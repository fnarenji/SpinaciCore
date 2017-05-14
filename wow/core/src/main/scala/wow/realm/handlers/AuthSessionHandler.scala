package wow.realm.handlers

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scodec.bits.ByteVector
import scodec.codecs._
import wow.auth.data.Account
import wow.realm.RealmServer.CreateSession
import wow.realm.protocol._
import wow.realm.protocol.payloads.{ClientAuthSession, ServerAuthResponse, ServerAuthResponseSuccess}
import wow.realm.session.NetworkWorker
import wow.utils.BigIntExtensions._
import wow.visualizer.{VisualMailboxMetric, VisualizerAPI}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Handles realm auth session packet
  */
object AuthSessionHandler extends PayloadHandler[NetworkWorker, ClientAuthSession] {
  protected override def handle(header: ClientHeader, payload: ClientAuthSession)(self: NetworkWorker): Unit = {
    import self._

    val login = payload.login

    Account.findByLogin(login) match {
      case Some(Account(_, _, _, Some(sessionKey))) =>
        def longLBytes(value: Long) = uint32L.encode(value).require.toByteArray

        val messageDigest = MessageDigest.getInstance("SHA-1")

        messageDigest.update(login.getBytes(StandardCharsets.US_ASCII))
        messageDigest.update(longLBytes(0))
        messageDigest.update(longLBytes(payload.challenge))
        messageDigest.update(longLBytes(authSeed))
        messageDigest.update(sessionKey.toUnsignedLBytes())
        val expectedDigest = messageDigest.digest()

        val view = ByteVector.view(expectedDigest)
        val response = if (payload.shaDigest === view) {
          // Same as for ClientPlayerLogin, we must wait to have the reference so that we're certain we have it as the
          // next packets will potentially be forwarded to the session for handling
          implicit val timeout = Timeout(5 seconds)

          // TODO: check for online
          val msg = CreateSession(login, context.self)
          VisualizerAPI.msg += VisualMailboxMetric(context.self.path.toStringWithoutAddress, realm.serverRef.path.toStringWithoutAddress, msg.hashCode(), msg.toString)
          val createSession = (realm.serverRef ? msg).mapTo[ActorRef]

          session = Await.result(createSession, 5 seconds)

          enableCipher(sessionKey)
          ServerAuthResponse(AuthResponses.Ok, Some(ServerAuthResponseSuccess(None)))
        } else {
          terminateDelayed()
          ServerAuthResponse(AuthResponses.UnknownAccount, None)
        }

        sendPayload(response)
      case _ =>
        val response = ServerAuthResponse(AuthResponses.UnknownAccount, None)

        sendPayload(response)
        terminateDelayed()
    }
  }
}

