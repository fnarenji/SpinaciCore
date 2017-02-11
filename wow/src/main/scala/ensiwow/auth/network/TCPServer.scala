package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.event.Logging
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import akka.testkit.TestActorRef

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yanncolina on 08/02/17.
  * This class defines the behaviour of the main TCP server.
  * @constructor send a Bind command to the TCP manager
  */

case class GetBufferSize()
case class GetData()
case class GetAddress()

class TCPServer extends Actor {
    import context.system

    implicit val timeout : Timeout = 2 seconds

    val log = Logging.getLogger(context.system, this)
    var handlerRef : ActorRef = null
    var address = ""

    log.debug("[TCPSERVER] Binding server with socket")
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 5555))

    override def postStop(): Unit = log.info("[TCPSERVER] Stopped")

    def receive = {

        case 42         => sender() ! 42
        case GetAddress => sender() ! address
        case GetData    =>
            log.debug("[TCPSERVER] Retrieving data stored in the handler's buffer")
            try {
                val future: Future[ByteString] = (handlerRef ? "getData").mapTo[ByteString]
                log.debug(s"[TCPSERVER] Retrieved: $future")
                if (future.isCompleted) {
                    val Success(data: ByteString) = future.value.get
                    sender() ! data
                }
            } catch {
                case EmptyBufferException(msg) => println(s"$msg")
            }

        case GetBufferSize =>
            log.debug("[TCPSERVER] Getting buffer size")
            val future: Future[Int] = (handlerRef ? GetBufferSize).mapTo[Int]
            future onComplete {
                case Success(size: Int) =>
                    log.debug(s"[TCPSERVER] Buffer size received: $size")
                    sender() ! size
                case Failure(t) =>
                    log.debug("[TCPTSERVER] An error has occured: " + t.getMessage)
            }

        case b @ Bound(localAddress) =>
            log.debug("[TCPSERVER] TCP port opened at: " + localAddress.getPort())
            address = localAddress.getHostString()

        case c @ Connected(remote, local) =>
            log.debug("[TCPSERVER] Remote connection set from: " + local + " to: " + remote)
            val connection = sender()
            handlerRef = context.actorOf(Props(classOf[BasicHandler], connection))
            connection ! Register(handlerRef)

        case CommandFailed(_: Bind) => context stop self
    }
}

