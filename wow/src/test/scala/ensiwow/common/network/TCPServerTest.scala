package ensiwow.common.network

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import ensiwow.auth.session.AuthSession
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Testing the TCP server
  */
class TCPServerTest extends AsyncFlatSpec with Matchers {
  implicit val timeout = Timeout(2 second)
  implicit val system = ActorSystem()

  val expectedHostname = "127.0.0.1"
  val expectedPort = 3725 // Use a different but close port in case an authserver is already running on the machine
  val expectedAddress = new InetSocketAddress(expectedHostname, expectedPort)

  val serverRef: ActorRef = TestActorRef(TCPServer.props(AuthSession, expectedHostname, expectedPort))

  "A server" must "be bound when created" in {
    val future = (serverRef ? GetAddress).mapTo[InetSocketAddress]
    future map { _ shouldEqual expectedAddress }
  }
}
