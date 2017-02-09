package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, Terminated}
import akka.event.Logging
import akka.io.{IO, Tcp}
import akka.io.Tcp._


/**
  * Created by yanncolina on 08/02/17.
  * This class defines the behaviour of the main TCP server.
  * @constructor send a Bind command to the TCP manager
  */
class TCPServer extends Actor {
    import context.system

    val log = Logging.getLogger(context.system, this)
    var address = ""

    log.info("Binding server with socket")
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 5555))

    override def postStop(): Unit = log.info("TCPServer is being stopped")

    def receive = {

        case 42 => sender() ! 42

        case "address?" => sender() ! address

        case b @ Bound(localAddress) =>
            log.info("TCP port opened")
            log.info("IP address: " + localAddress.getHostString())
            log.info("Port opened: " + localAddress.getPort())
            address = localAddress.getHostString()

        case c @ Connected(remote, local) =>
            log.info("Remote connection set")

        case CommandFailed(_: Bind) => context stop self

    }
}

