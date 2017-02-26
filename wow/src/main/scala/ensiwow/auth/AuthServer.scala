package ensiwow.auth

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.auth.handlers.{LogonChallengeHandler, LogonProofHandler, ReconnectChallengeHandler, ReconnectProofHandler}
import ensiwow.auth.protocol.{ServerPacket, VersionInfo}
import ensiwow.auth.protocol.packets.{ServerRealmlistPacket, ServerRealmlistPacketEntry}
import ensiwow.auth.session.{AuthSession, EventRealmlist}
import ensiwow.common.network.TCPServer
import ensiwow.utils.PacketSerializationException
import scodec.Attempt.{Failure, Successful}
import scodec.Codec
import scodec.bits.BitVector

case object GetRealmlist

/**
  * AuthServer is the base actor for all services provided by the authentication server.
  *
  * This actor is unique (e.g. singleton) per ActorSystem.
  * It holds ownership of the stateless packet handlers.
  */
class AuthServer extends Actor with ActorLogging {
  val address = "127.0.0.1"
  val port = 3724

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  // TODO: should handlers be put in a pool so that they scale accordingly to the load ?
  context.actorOf(LogonChallengeHandler.props, LogonChallengeHandler.PreferredName)
  context.actorOf(LogonProofHandler.props, LogonProofHandler.PreferredName)
  context.actorOf(ReconnectChallengeHandler.props, ReconnectChallengeHandler.PreferredName)
  context.actorOf(ReconnectProofHandler.props, ReconnectProofHandler.PreferredName)
  context.actorOf(TCPServer.props(AuthSession, address, port), TCPServer.PreferredName)

  // TODO: factorize
  private def serialize[T <: ServerPacket](value: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(value) match {
      case Successful(bits) => bits
      case Failure(err) => throw PacketSerializationException(err)
    }
  }

  // TODO: find a way to retrieve address and port
  private val realms = Vector(ServerRealmlistPacketEntry(1, 0, 0, "EnsiWoW", "127.0.0.1:8085", 0, 1, 1, 1))
  private val serverRealmlistPacket: ServerRealmlistPacket = ServerRealmlistPacket(realms)
  private val eventRealmlist: EventRealmlist= EventRealmlist(serialize(serverRealmlistPacket))

  override def receive: PartialFunction[Any, Unit] = {
    case GetRealmlist => sender() ! eventRealmlist
  }
}

object AuthServer {
  def props: Props = Props(new AuthServer)

  val PreferredName = "AuthServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
  val LogonProofHandlerPath = s"$ActorPath/${LogonProofHandler.PreferredName}"
  val ReconnectChallengeHandlerPath = s"$ActorPath/${ReconnectChallengeHandler.PreferredName}"
  val ReconnectProofHandlerPath = s"$ActorPath/${ReconnectProofHandler.PreferredName}"
}

