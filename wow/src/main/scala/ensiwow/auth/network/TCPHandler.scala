package ensiwow.auth.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.{Close, ConnectionClosed, Received, Write}
import akka.util.ByteString
import ensiwow.auth.session.{AuthSession, EventPacket}
import scodec.bits.BitVector
import scodec.interop.akka._

case class OutgoingPacket(bits: BitVector)

case object Disconnect

/**
  * Created by yanncolina on 10/02/17.
  */
class TCPHandler(connection: ActorRef) extends Actor with ActorLogging {
  val authSession = context.actorOf(AuthSession.props, AuthSession.PreferredName)

  def receive = {
    case Received(data: ByteString) =>
      log.debug(s"Received: $data")
      authSession ! EventPacket(data.toByteVector.bits)

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
  def props(connection: ActorRef): Props = Props(new TCPHandler(connection))

  def PreferredName(inetSocketAddress: InetSocketAddress) =
    s"Handler@${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}"
}
