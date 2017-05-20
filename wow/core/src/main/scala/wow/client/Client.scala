package wow.client


import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.{IO, Tcp}
import wow.Application
import wow.client.auth._
import scodec.interop.akka._

class Client(remote: InetSocketAddress) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  val authMachine = AuthMachine(AuthState.NoState, None)

  IO(Tcp) ! Connect(remote)

  def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(_: Connect) =>
      context stop self

    case Connected =>
      val connection = sender()
      connection ! Register(self)
      AuthMachine.transition(authMachine, EventAuthenticate())
      authMachine.outgoingData foreach (connection ! _)

      context become {
        case Received(data) =>
          AuthMachine.transition(authMachine, EventIncoming(data.toByteVector.bits))
          authMachine.outgoingData foreach (connection ! _)
        case _: ConnectionClosed =>
          context stop self
      }
  }
}

object Client {
  def props(remote: InetSocketAddress) = Props(new Client(remote))

  def PreferredName = "client"

  def ActorPath = s"${Application.ActorPath}/$PreferredName"
}

