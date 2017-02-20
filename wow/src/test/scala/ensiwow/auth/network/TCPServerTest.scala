package ensiwow.auth.network

import org.scalatest.FlatSpec
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Testing the TCP server
  */
class TCPServerTest extends FlatSpec {
  implicit val timeout = Timeout(2 second)
  implicit val system = ActorSystem()

  val serverRef: ActorRef = TestActorRef(new TCPServer)
  "A server" must "be binded when created" in {
    val future = (serverRef ? GetAddress).mapTo[String]
    future onComplete {
      case Success(address) => assert(address === "127.0.0.1")
      case Failure(t)       => assert(false)
    }
  }
}
