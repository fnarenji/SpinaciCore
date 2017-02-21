package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

case object GetSocketAddress

/**
  * This class defines the behaviour of the main TCP server.
  *
  * @constructor send a Bind command to the TCP manager
  */
class TCPServer extends Actor with ActorLogging {
  val bindAddress = "127.0.0.1"
  val bindPort = 3724

  var address: InetSocketAddress = _

  log.debug("Binding server with socket")
  IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(bindAddress, bindPort))

  override def postStop: Unit = log.debug("Stopped")

  def receive: PartialFunction[Any, Unit] = {
    case GetSocketAddress => sender() ! address

    case Bound(localAddress) =>
      log.debug(s"TCP port opened at: ${localAddress.getHostString}:${localAddress.getPort}")
      address = localAddress

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


