package ensiwow.common.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

case object GetAddress

/**
  * This class defines the behaviour of the main TCP server.
  *
  * @constructor send a Bind command to the TCP manager
  */
class TCPServer[T <: Session](val companion: T, val address: String, val port: Int) extends Actor with ActorLogging {
  log.debug("Binding server with socket")
  IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(address, port))

  override def postStop(): Unit = log.debug("Stopped")

  var boundAddress: InetSocketAddress = _

  def receive: PartialFunction[Any, Unit] = {
    case GetAddress => sender() ! boundAddress

    case Bound(localAddress) =>
      log.debug(s"TCP port opened at: ${localAddress.getHostString}:${localAddress.getPort}")
      boundAddress = localAddress

    case Connected(remote, local) =>
      log.debug(s"Remote connection set from: $local to: $remote")
      val handlerRef = context.actorOf(TCPHandler.props(companion, sender), TCPHandler.PreferredName(remote))
      sender ! Register(handlerRef)

    case CommandFailed(_: Bind) => context stop self
  }
}

object TCPServer {
  def props[T <: Session](companion: T, address: String, port: Int): Props = Props(classOf[TCPServer[T]],
    companion,
    address,
    port)

  val PreferredName = "TCP"
}


