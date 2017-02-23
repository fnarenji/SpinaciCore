package ensiwow.common.network

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import ensiwow.auth.session.AuthSession
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Testing the TCP server
  */
class TCPServerTest extends FlatSpec {
  implicit val timeout = Timeout(2 second)
  implicit val system = ActorSystem()

  val address = "127.0.0.1"
  val port = 3724

  val serverRef: ActorRef = TestActorRef(TCPServer.props(AuthSession, address, port))
  "A server" must "be bound when created" in {
    val future = (serverRef ? GetAddress).mapTo[InetSocketAddress]
    future onComplete {
      case Success(boundAddress) => assert(boundAddress.getHostString + ":" + boundAddress.getPort === "127.0.0.1:3724")
      case Failure(t) => fail(t)
    }
  }
}
