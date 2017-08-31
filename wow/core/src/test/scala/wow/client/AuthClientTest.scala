package wow.client

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import org.scalatest.WordSpec
import wow.auth.AuthServer
import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.ServerPacket
import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerRealmlist}
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
  val authClient = new AuthClient(system, )
  val tcpClient: ActorRef = system.actorOf(TcpClient.props(authClient.pool), TcpClient.PreferredNamed)
  Thread.sleep(2000)

  var futureChallenge: Future[ServerLogonChallenge] = _

  "An authentication client" when {
    "queried with a valid login, it must answer with a challenge" in {
      authClient.execute(new SendChallenge(Vector(127, 0, 0, 1), "t"), tcpClient)
      futureChallenge = authClient.await(AuthOpCodes.ServerLogonChallenge).asInstanceOf[Future[ServerLogonChallenge]]

      val challenge: ServerLogonChallenge = Await.result(futureChallenge, timeout.duration)
      assert(challenge.authResult === AuthResult(0))
    }
    "later on, when queried with a valid proof, it must answer with a server proof" in {
      authClient.execute(new SendProof(AccountEntry("t", "t")), tcpClient, Some(futureChallenge))
      val f = authClient.await(AuthOpCodes.ServerLogonProof).asInstanceOf[Future[ServerLogonProof]]

      val proof: ServerLogonProof = Await.result(f, timeout.duration)
      assert(proof.authResult === AuthResult(0))
    }
    "next, when it receives a realmlist request, it must send back the list of the available realms" in {
      authClient.execute(new SendRealmlistRequest, tcpClient)
      val f: Future[ServerPacket] = authClient.await(AuthOpCodes.ServerRealmlist)
      val realmlist: ServerRealmlist = Await.result(f, timeout.duration).asInstanceOf[ServerRealmlist]

      assert(realmlist.realms.head.name === "Realm 1")
    }
  }

}
