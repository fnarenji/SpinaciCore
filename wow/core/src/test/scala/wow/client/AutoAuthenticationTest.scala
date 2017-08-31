package wow.client

import akka.actor.{ActorRef, ActorSystem}
import org.scalatest.{Matchers, WordSpec}
import wow.auth.AuthServer
import wow.auth.protocol.ServerPacket
import wow.client.auth._

import scala.concurrent.Promise

class AutoAuthenticationTest extends WordSpec with Matchers {

  implicit val system = ActorSystem("wow")

  val operations = (new SendChallenge(Vector(127,0,0,1), "t"), Promise[ServerPacket]) ::
    (new SendProof(AccountEntry("t", "t")), Promise[ServerPacket]) ::
    (new SendRealmlistRequest, Promise[ServerPacket]) :: Nil

  system.actorOf(AuthServer.props, AuthServer.PreferredName)

  val authClient = new AuthClient(system)
  val tcpClient: ActorRef = system.actorOf(TcpClient.props(authClient.pool), TcpClient.PreferredNamed)

  authClient.execute(operations, tcpClient)
}
