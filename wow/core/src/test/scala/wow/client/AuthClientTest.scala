package wow.client

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import org.scalatest.WordSpec
import wow.auth.AuthServer
import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.OpCodes
import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonChallengeSuccess, ServerLogonProof, ServerRealmlist}
import wow.client.auth._
import wow.realm.RealmServer

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AuthClientTest extends WordSpec {
  implicit val system = ActorSystem("wow")
  implicit val timeout = Timeout(5 seconds)

  system.actorOf(AuthServer.props, AuthServer.PreferredName)
  system.actorOf(RealmServer.props(1), RealmServer.PreferredName(1))

  Thread.sleep(1000)
  val authClient = new AuthClient
  val tcpClient: ActorRef = system.actorOf(TcpClient.props(authClient), TcpClient.PreferredNamed)
  Thread.sleep(1000)

  var challenge: ServerLogonChallengeSuccess = _

  "An authentication client" when {
    "queried with a challenge request" should {
      "answer with a challenge" in {
        authClient.execute(new SendChallenge(Vector(127, 0, 0, 1), "t"), tcpClient)
        val future: Future[ServerLogonChallenge] = authClient.await(OpCodes.LogonChallenge).mapTo[ServerLogonChallenge]
        val challengeResponse: ServerLogonChallenge = Await.result(future, timeout.duration)
        assert(challengeResponse.authResult === AuthResult(0))
        challengeResponse.success foreach (challenge = _)
      }
    }
    "later on, when queried with a proof" should {
      "answer with a server proof" in {
        authClient.execute(new SendProof(AccountEntry("t", "t"), challenge), tcpClient)
        val future: Future[ServerLogonProof] = authClient.await(OpCodes.LogonProof).mapTo[ServerLogonProof]
        val proofResponse: ServerLogonProof = Await.result(future, timeout.duration)
        assert(proofResponse.authResult === AuthResult(0))
      }
    }
    "next, when it receives a realmlist request" should {
      "send back the list of the available realms" in {
        authClient.execute(new SendRealmlistRequest, tcpClient)
        val future: Future[ServerRealmlist] = authClient.await(OpCodes.RealmList).mapTo[ServerRealmlist]
        val realmlist: ServerRealmlist = Await.result(future, timeout.duration)
        assert(realmlist.realms.head.name === "Realm 1")
      }
    }
  }
}
