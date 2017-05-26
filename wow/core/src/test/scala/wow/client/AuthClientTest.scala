package wow.client

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import org.scalatest.WordSpec
import wow.auth.AuthServer
import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.ServerPacket
import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonChallengeSuccess, ServerLogonProof, ServerRealmlist}
import wow.client.auth._
import wow.realm.RealmServer

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class AuthClientTest extends WordSpec {
  implicit val system = ActorSystem("wow")
  implicit val timeout = Timeout(5 seconds)

  system.actorOf(AuthServer.props, AuthServer.PreferredName)
  system.actorOf(RealmServer.props(1), RealmServer.PreferredName(1))

  Thread.sleep(2000)
  val authClient = new AuthClient(system)
  val tcpClient: ActorRef = system.actorOf(TcpClient.props(authClient.pool, authClient.buffer), TcpClient.PreferredNamed)
  Thread.sleep(2000)

  var challenge: ServerLogonChallengeSuccess = _

  "An authentication client" when {
    "queried with a challenge request" should {
      "answer with a challenge" in {
        authClient.execute(new SendChallenge(Vector(127, 0, 0, 1), "t"), tcpClient)
        val f1: Future[ServerPacket] = authClient.await(AuthOpCodes.ServerLogonChallenge)
        val serverChallenge: ServerLogonChallenge = Await.result(f1, timeout.duration).asInstanceOf[ServerLogonChallenge]
        assert(serverChallenge.authResult === AuthResult(0))

        serverChallenge.success foreach (challenge = _)
      }
    }
    "later on, when queried with a proof" should {
      "answer with a server proof" in {
        authClient.execute(new SendProof(AccountEntry("t", "t"), challenge), tcpClient)
        val f2: Future[ServerPacket] = authClient.await(AuthOpCodes.ServerLogonProof)
        val proof: ServerLogonProof = Await.result(f2, timeout.duration).asInstanceOf[ServerLogonProof]
        assert(proof.authResult === AuthResult(0))
      }
    }
    "next, when it receives a realmlist request" should {
      "send back the list of the available realms" in {
        authClient.execute(new SendRealmlistRequest, tcpClient)
        val f3: Future[ServerPacket] = authClient.await(AuthOpCodes.ServerRealmlist)
        val realmlist: ServerRealmlist = Await.result(f3, timeout.duration).asInstanceOf[ServerRealmlist]
        assert(realmlist.realms.head.name === "Realm 1")
      }
    }
  }
}
