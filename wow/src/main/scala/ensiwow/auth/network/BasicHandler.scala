package ensiwow.auth.network

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString

/**
  * Created by yanncolina on 10/02/17.
  */

object BasicHandler {
    def props(connection: ActorRef): Props = Props(new BasicHandler(connection))
}

class BasicHandler(connection: ActorRef) extends Actor with ActorLogging {
    def receive = {
        case Received(data: ByteString) =>
            log.debug("[HANDLER] Received: " + data)
            // send to SessionActor

        case PeerClosed => context stop self
    }
}

