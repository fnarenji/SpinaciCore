package wow.client


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import wow.client.auth._
import scodec.interop.akka._

case class StartAuthentication()

case class ServerReady(port: Int)

/**
  * A client that should behave like a real one
  *
  * @param remote a client is defined by the port and the hostname through which it communicates
  *               with the server
  */
class Client(remote: InetSocketAddress) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  log.debug("Starting the client")
  var authMachine = AuthMachine(AuthState.NoState, None)

  IO(Tcp) ! Connect(remote)

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      context stop self

    case Connected(_, _) =>
      val connection = sender()
      connection ! Register(self)
      context become {
        case StartAuthentication =>
          authMachine = AuthMachine.transition(authMachine, EventAuthenticate())
          authMachine.outgoingData foreach (connection ! _)
        case data: ByteString =>
          connection ! Write(data)
        case Received(data) =>
          log.debug(s"Received some data: $data")
          authMachine = AuthMachine.transition(authMachine, EventIncoming(data.toByteVector.bits))
          // authMachine.outgoingData foreach { d => println(s"Sending data: "+ d.toString) }
          authMachine.outgoingData foreach (connection ! _)
        case _: ConnectionClosed =>
          context stop self
      }
  }
}

object Client {
  def props(remote: InetSocketAddress) = Props(new Client(remote))

  val PreferredName = "client"
}

