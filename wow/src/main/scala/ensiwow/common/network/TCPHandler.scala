package ensiwow.common.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{Close, ConnectionClosed, Received, Write}
import akka.util.ByteString
import scodec.bits.BitVector
import scodec.interop.akka._

case class OutgoingPacket(bits: BitVector)

case object Disconnect

/**
  * Handles an open TCP connection.
  */
class TCPHandler[T <: Session](companion: T, connection: ActorRef) extends Actor with ActorLogging {
  private val session = context.actorOf(companion.props, companion.PreferredName)

  def receive: PartialFunction[Any, Unit] = {
    case Received(data: ByteString) =>
      log.debug(s"Received: $data")
      session ! EventPacket(data.toByteVector.bits)

    case OutgoingPacket(bits) =>
      val byteString = bits.bytes.toByteString
      log.debug(s"Sending: $byteString")
      connection ! Write(byteString)

    // Flushes pending writes and gracefully closes the connection
    case Disconnect =>
      log.debug("Closing")
      connection ! Close

    case closed: ConnectionClosed =>
      log.debug(s"Connection closed ($closed)")
      context stop self
  }
}

object TCPHandler {
  def props[T <: Session](companion: T, connection: ActorRef): Props = Props(new TCPHandler(companion, connection))

  def PreferredName(inetSocketAddress: InetSocketAddress) =
    s"Handler@${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}"
}