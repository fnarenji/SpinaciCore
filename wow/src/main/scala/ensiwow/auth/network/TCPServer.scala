package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.Actor
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

    log.info("Binding server with socket")
    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 5555))

    def receive = {
        case b @ Bound(localAddress) =>
            assert(localAddress.getHostName == "127.0.0.1")
            log.info("TCP port opened")
            log.info("IP address: " + localAddress.getHostString())
            log.info("Port opened: " + localAddress.getPort())

        case c @ Connected(remote, local) =>
            log.info("Remote connection set")

        case CommandFailed(_: Bind) => context stop self

    }
}

