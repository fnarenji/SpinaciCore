package ensiwow.auth.network

import org.scalatest.FlatSpec
import akka.testkit.TestActorRef
import akka.pattern.ask

import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yanncolina on 09/02/17.
  */

case class ServerNotBoundException(msg: String) extends Exception(msg)
class TCPServerTest extends FlatSpec {

    implicit val timeout : Timeout = 2 seconds
    implicit val system = ActorSystem()

    val serverRef = TestActorRef(new TCPServer)

    "A server" should "be binded when created" in {
        val future: Future[String] = (serverRef ? GetAddress).mapTo[String]
        future onComplete {
            case Success(result: String) => assert(result === "127.0.0.1")
            case Failure(t)              => fail("Server not bound")
        }
    }
}
