package network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString


object Client {
    def props(remote: InetSocketAddress, listener: ActorRef) = Props(classOf[Client], remote, listener)
}

/**
  * Created by yanncolina on 08/02/17.
  * A client class which implements basic features in order to test the TCP server.
  * @constructor send a Connect message to the TCP manager
  * @param remote the ip address and port of the socket.
  * @param listener the server's actor reference to which the messages are sent and received.
  */
class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {
    import akka.io.Tcp._
    import context.system

    IO(Tcp) ! Connect(remote)

    def receive = {
        case CommandFailed(_: Connect) =>
            listener ! "connect failed"
            context stop self

        case c @ Connected(remote, local) =>
            listener ! c
            val connection = sender()
            connection ! Register(self)
            context become {
                case data: ByteString =>
                    connection ! Write(data)
                case CommandFailed(w: Write) =>
                    // O/S buffer was full
                    listener ! "write failed"
                case Received(data) =>
                    listener ! data
                case "close" =>
                    connection ! Close
                case _: ConnectionClosed =>
                    listener ! "connection closed"
                    context stop self
            }
    }
}
