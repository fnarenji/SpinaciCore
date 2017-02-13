package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import akka.util.Timeout

import scala.concurrent.duration._

/**
  * Created by yanncolina on 08/02/17.
  * This class defines the behaviour of the main TCP server.
  * @constructor send a Bind command to the TCP manager
  */

case class GetAddress()
class TCPServer extends Actor with ActorLogging {
    import context.system

    implicit val timeout : Timeout = 2 seconds

    var address = ""

    log.debug("[TCPSERVER] Binding server with socket")
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 3724))

    override def postStop(): Unit = log.info("[TCPSERVER] Stopped")

    def receive = {

        case GetAddress => sender() ! address

        case Bound(localAddress) =>
            log.debug("[TCPSERVER] TCP port opened at: " + localAddress.getPort)
            address = localAddress.getHostString

        case Connected(remote, local) =>
            log.debug("[TCPSERVER] Remote connection set from: " + local + " to: " + remote)
            val connection = sender()
            val handlerRef: ActorRef = context.actorOf(BasicHandler.props(connection))
            connection ! Register(handlerRef)

        case CommandFailed(_: Bind) => context stop self
    }
}

