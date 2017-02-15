package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by yanncolina on 08/02/17.
  * This class defines the behaviour of the main TCP server.
  *
  * @constructor send a Bind command to the TCP manager
  */

case class GetAddress()

class TCPServer extends Actor with ActorLogging {
  val bindAddress = "127.0.0.1"
  val bindPort = 3724

  implicit val timeout: Timeout = 2 seconds

  var address = ""

  log.debug("Binding server with socket")
  IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(bindAddress, bindPort))

  override def postStop(): Unit = log.debug("Stopped")

  def receive = {

    case GetAddress => sender() ! address

    case Bound(localAddress) =>
      log.debug(s"TCP port opened at: ${localAddress.getPort}")
      address = localAddress.getHostString

    case Connected(remote, local) =>
      log.debug(s"Remote connection set from: $local to: $remote")
      val handlerRef = context.actorOf(TCPHandler.props(sender), TCPHandler.PreferredName(remote))
      sender ! Register(handlerRef)

    case CommandFailed(_: Bind) => context stop self
  }
}

object TCPServer {
  def props: Props = Props(new TCPServer)

  val PreferredName = "TCP"
}


