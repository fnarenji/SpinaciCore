package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.util.ByteString

import scala.collection.mutable.ListBuffer

/**
  * Created by yanncolina on 10/02/17.
  */
case class EmptyBufferException(message: String) extends Exception(message)

class BasicHandler(connection: ActorRef) extends Actor {
    val log = Logging.getLogger(context.system, this)

    def receive = {
        case Received(data: ByteString) =>
            log.debug("[HANDLER] Received: " + data)
            // send to SessionActor

        case PeerClosed => context stop self
    }
}

