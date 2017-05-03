package ensiwow.common.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{Close, ConnectionClosed, Received, Write}
import akka.util.ByteString
import scodec.bits.BitVector
import scodec.interop.akka._

/**
  * Handles an open TCP connection.
  */
class TCPHandler[T <: SessionActorCompanion](companion: T, connection: ActorRef) extends Actor with ActorLogging {
  private val session = context.actorOf(companion.props, companion.PreferredName)

  def receive: PartialFunction[Any, Unit] = {
    case Received(data: ByteString) =>
      log.debug(s"Received: ${data.toByteVector.toHex}")
      session ! EventIncoming(data.toByteVector.bits)

    case TCPHandler.OutgoingPacket(bits) =>
      val byteString = bits.bytes.toByteString
      log.debug(s"Sending: ${bits.toHex}")
      connection ! Write(byteString)

    // Flushes pending writes and gracefully closes the connection
    case TCPHandler.Disconnect =>
      log.debug("Closing")
      connection ! Close

    case closed: ConnectionClosed =>
      log.debug(s"Connection closed ($closed)")
      context stop self
  }
}

object TCPHandler {
  def props[T <: SessionActorCompanion](companion: T, connection: ActorRef): Props = Props(classOf[TCPHandler[T]], companion, connection)

  def PreferredName(inetSocketAddress: InetSocketAddress) =
    s"Handler@${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}"

  case class OutgoingPacket(bits: BitVector)

  case object Disconnect
}
