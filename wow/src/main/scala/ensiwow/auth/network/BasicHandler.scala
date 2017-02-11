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
    private var storage = ListBuffer.empty[ByteString]
    private val maxStored = 100

    case object Ack extends Event

    def receive = {
        case Received(data: ByteString) =>
            log.debug("[HANDLER] Received: " + data)
            buffer(data)

        case PeerClosed    => context stop self
        case GetData       => sender() ! getData
        case GetBufferSize => sender() ! getBufferSize
    }

    private def buffer(data: ByteString): Unit = {
        storage :+= data
        log.debug(s"[HANDLER] Stored: $data. Buffer size: ${storage.size}")

        if (storage.size > maxStored) {
            log.warning(s"drop connection (buffer overrun)")
            context stop self
        }
    }

    def getBufferSize: Int = storage.size

    def getData: ByteString = {
        log.debug("[HANDLER] Retrieving data from buffer")
        if (!storage.isEmpty) storage.take(1).head
        else throw EmptyBufferException("The server's buffer is empty")
    }
}

