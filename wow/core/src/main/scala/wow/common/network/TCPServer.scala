package wow.common.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props, SupervisorStrategy}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

case object GetAddress

/**
  * This class defines the behaviour of the TCP server.
  *
  * @constructor send a Bind command to the TCP manager
  */
class TCPServer[A <: TCPSessionFactory](val factory: A, val address: String, val port: Int)
  extends Actor with ActorLogging {
  log.debug("Binding server with socket")
  IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(address, port))

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def postStop(): Unit = log.debug(s"Stopped TCP server for $address:$port")

  def receive: PartialFunction[Any, Unit] = {
    case Bound(localAddress) =>
      log.debug(s"TCP port opened at: ${localAddress.getHostString}:${localAddress.getPort}")

    case Connected(remote, local) =>
      log.debug(s"Remote connection set from $remote to $local")
      val handlerRef = context.actorOf(factory.props(sender), factory.PreferredName + TCPSession.PreferredName(remote))
      sender ! Register(handlerRef)

    case CommandFailed(_: Bind) => context stop self
  }
}

object TCPServer {
  def props[A <: TCPSessionFactory](companion: A, address: String, port: Int): Props = Props(classOf[TCPServer[A]],
    companion,
    address,
    port)

  val PreferredName = "tcp"
}


