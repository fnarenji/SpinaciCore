package ensiwow.auth.network

import org.scalatest.FlatSpec
import akka.testkit.TestActorRef
import akka.pattern.ask

import scala.util.Success
import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ Future, Await }

/**
  * Created by yanncolina on 09/02/17.
  */
class TCPServerTest extends FlatSpec {

    implicit val timeout : Timeout = 2 seconds
    implicit val system = ActorSystem()

    val serverRef = TestActorRef(new TCPServer)

    "A server" should "be binded when created" in {
        val future : Future[String] = (serverRef ? "address?").mapTo[String]
        val Success(result: String) = future.value.get
        assert(result === "127.0.0.1")
    }

    it should "return 42 when 42 is sent to it" in {
        val future : Future[Int] = (serverRef ? 42).mapTo[Int]
        val Success(result: Int) = future.value.get
        assert(result === 42)
    }
}
