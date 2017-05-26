package wow.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.Timeout
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, DecodeResult}
import scodec.bits.BitVector
import scodec.interop.akka._
import wow.Application
import wow.auth.AuthServerConfiguration
import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerRealmlist}
import wow.auth.protocol.{OpCodes, ServerPacket}
import wow.auth.utils.MalformedPacketHeaderException
import wow.client.auth.{AuthOpCodes, PacketSerializer}

import scala.collection.parallel.immutable.ParVector
import scala.concurrent.duration._

case class NewPacket[A <: ServerPacket](packet: A)
/**
  * A TcpClient instance is the interface between the tested client with the servers
  */
class TcpClient(pool: Map[AuthOpCodes.Value, ActorRef], var buffer: ParVector[BitVector]) extends Actor with ActorLogging {

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
          log.debug(s"Got a message from the server: $data")
          val bits = data.toByteVector.bits
          buffer = buffer.+:(bits)
          Codec[OpCodes.Value].decode(bits) match {
            case Successful(DecodeResult(OpCodes.LogonChallenge, _)) =>
              log.debug("Got a logon challenge")
              pool(AuthOpCodes.ServerLogonChallenge) ! NewPacket(PacketSerializer.deserialize(bits)(ServerLogonChallenge.codec))
            case Successful(DecodeResult(OpCodes.RealmList, _)) =>
              log.debug("Got a realmlist packet")
              pool(AuthOpCodes.ServerRealmlist) ! NewPacket(PacketSerializer.deserialize(bits)(ServerRealmlist.codec))
            case Successful(DecodeResult(OpCodes.LogonProof, _)) =>
              log.debug("Got a proof packet")
              pool(AuthOpCodes.ServerLogonProof) ! NewPacket(PacketSerializer.deserialize(bits)(ServerLogonProof.codec))
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
  def props(pool: Map[AuthOpCodes.Value, ActorRef], buffer: ParVector[BitVector]) = Props(new TcpClient(pool, buffer))

  val PreferredNamed = "tcpClient"
}

