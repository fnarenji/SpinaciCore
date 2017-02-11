package ensiwow.auth.network

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import org.scalatest.FlatSpec
import akka.testkit.TestActorRef
import akka.pattern.ask

import scala.util.{Failure, Success}
import akka.actor.{ActorRef, ActorSystem}
import akka.util.{ByteString, Timeout}
import com.sun.javaws.exceptions.InvalidArgumentException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by yanncolina on 09/02/17.
  */
class TCPServerTest extends FlatSpec  {

    implicit val timeout : Timeout = 5 seconds
    implicit val system = ActorSystem()

    val serverRef = TestActorRef(new TCPServer)

    "A server" should "be binded when created" in {
        val future : Future[String] = (serverRef ? GetAddress).mapTo[String]
        val Success(result: String) = future.value.get
        assert(result === "127.0.0.1")
    }

    it should "return 42 when 42 is sent to it" in {
        val future : Future[Int] = (serverRef ? 42).mapTo[Int]
        val Success(result: Int) = future.value.get
        assert(result === 42)
    }

    val clientRef = TestActorRef(new Client(new InetSocketAddress("localhost", 5555), serverRef))

    it should "be able to receive data from a client" in {
        def getBufferSize: Unit = {
            import scala.concurrent.ExecutionContext.Implicits.global
            val future: Future[Int] = (serverRef ? GetBufferSize).mapTo[Int]
            future onComplete {
                case Success(size) if size != 0 =>
                    println("Got a buffer size different than 0")
                    size
                case Success(size) if size == 0 =>
                    println("Got a buffer size of 0")
                    getBufferSize
                case Failure(t) =>
                    println("getBufferSize failed")
                    0
            }
        }

        val data = ByteString("hello world")
        clientRef ! data
        val bufferSize = getBufferSize
        assert(false)
        // assert(result === ByteString("hello world"))
    }
}
