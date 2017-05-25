package wow.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.Timeout
import scodec.interop.akka._
import wow.Application
import wow.auth.AuthServerConfiguration
import wow.client.auth.AuthClient

import scala.concurrent.duration._

class TcpClient[A <: TestTarget[A]](target: A) extends Actor with ActorLogging {

  implicit val timeout = new Timeout(5 seconds)
  val authConfig: AuthServerConfiguration = Application.configuration.auth

  IO(Tcp)(context.system) ! Connect(new InetSocketAddress(authConfig.host, authConfig.port))

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      context stop self

    case Connected(_, _) =>
      val connection = sender
      connection ! Register(self)
      context become {
        case Received(data) =>
          log.debug(s"Got a message from the server: $data")
          target.buffer = target.buffer.+:(data.toByteVector.bits)
        case data: Write =>
          log.debug(s"Sending a packet: $data")
          connection ! data
      }
    case b => log.debug(s"Got something strange: $b")
  }
}

object TcpClient {
  def props(client: AuthClient) = Props(new TcpClient(client))

  val PreferredNamed = "tcpClient"
}

