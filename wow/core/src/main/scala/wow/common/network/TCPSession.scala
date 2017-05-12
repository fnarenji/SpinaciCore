package wow.common.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.{Close, ConnectionClosed, Received, Write}
import akka.util.ByteString
import scodec.bits.BitVector
import scodec.interop.akka._

/**
  * Handles an open TCP connection.
  */
trait TCPSession extends Actor with ActorLogging {
  val connection: ActorRef

  /**
    * Handler for incoming data
    * @param data incoming data
    */
  def incoming(data: BitVector): Unit

  /**
    * Sends packet
    *
    * @param bits bits of packet
    */
  protected def outgoing(bits: BitVector): Unit = {
    val byteString = bits.bytes.toByteString
    log.debug(s"Sending: ${bits.toHex}")
    connection ! Write(byteString)
  }

  /**
    * Flushes pending writes and gracefully closes the connection
    */
  protected def disconnect(): Unit = {
    log.debug("Closing")
    connection ! Close
  }

  val tcpSessionReceiver: Receive = {
    case Received(data: ByteString) =>
      val byteVector = data.toByteVector
      log.debug(s"Received: ${byteVector.toHex}")
      incoming(byteVector.bits)

    case closed: ConnectionClosed =>
      log.debug(s"Connection closed ($closed)")
      context stop self
  }
}

object TCPSession {
  def PreferredName(inetSocketAddress: InetSocketAddress) =
    s"-${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}"
}

