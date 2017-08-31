package wow.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.Timeout
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult}
import scodec.interop.akka._
import wow.Application
import wow.auth.AuthServerConfiguration
import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerRealmlist}
import wow.auth.protocol.{OpCodes, ServerPacket}
import wow.auth.utils.MalformedPacketHeaderException
import wow.client.auth.{AuthOpCodes, PacketSerializer}

import scala.concurrent.duration._

case class NewPacket[A <: ServerPacket](packet: A)
/**
  * A TcpClient instance is the interface between the tested client with the servers
  */
class TcpClient(pool: Map[AuthOpCodes.Value, ActorRef]) extends Actor with ActorLogging {

  implicit val timeout = new Timeout(5 seconds)
  val authConfig: AuthServerConfiguration = Application.configuration.auth

  IO(Tcp)(context.system) ! Connect(new InetSocketAddress(authConfig.host, authConfig.port))

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      context stop self

    case Connected(_, _) =>
      val connection = sender
      connection ! Register(self)
      context become {
        case Received(data) =>
          val bits = data.toByteVector.bits
          Codec[OpCodes.Value].decode(bits) match {
            case Successful(DecodeResult(OpCodes.LogonChallenge, _)) =>
              val packet = PacketSerializer.deserialize(bits)(ServerLogonChallenge.codec)
              pool(AuthOpCodes.ServerLogonChallenge) ! NewPacket(packet)

            case Successful(DecodeResult(OpCodes.RealmList, _)) =>
              val packet = PacketSerializer.deserialize(bits)(ServerRealmlist.codec)
              pool(AuthOpCodes.ServerRealmlist) ! NewPacket(packet)

            case Successful(DecodeResult(OpCodes.LogonProof, _)) =>
              val packet = PacketSerializer.deserialize(bits)(ServerLogonProof.codec)
              pool(AuthOpCodes.ServerLogonProof) ! NewPacket(packet)

            case Failure(err) => throw MalformedPacketHeaderException(err)
            case _ => log.debug("Malformed packet")
          }
        case data: Write =>
          log.debug(s"Sending a packet: $data")
          connection ! data
      }
    case b => log.debug(s"Got something strange: $b")
  }

}

object TcpClient {
  def props(pool: Map[AuthOpCodes.Value, ActorRef]) = Props(new TcpClient(pool))

  val PreferredNamed = "tcpClient"
}

