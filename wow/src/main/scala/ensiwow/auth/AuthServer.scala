package ensiwow.auth

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.auth.handlers.{LogonChallengeHandler, LogonProofHandler, ReconnectChallengeHandler, ReconnectProofHandler}
import ensiwow.auth.protocol.packets.{ServerRealmlistPacket, ServerRealmlistPacketEntry}
import ensiwow.auth.session.{AuthSession, EventRealmlist}
import ensiwow.common.VersionInfo
import ensiwow.common.network.TCPServer
import ensiwow.utils.PacketSerializer

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

  // TODO: find a way to retrieve address and port
  private val realms = Vector(ServerRealmlistPacketEntry(1, 0, 0, "EnsiWoW", "127.0.0.1:8085", 0, 1, 1, 1))
  private val serverRealmlistPacket: ServerRealmlistPacket = ServerRealmlistPacket(realms)
  private val serverRealmlistPacketBits = PacketSerializer.serialize(serverRealmlistPacket)
  private val eventRealmlist: EventRealmlist = EventRealmlist(serverRealmlistPacketBits)

  override def receive: PartialFunction[Any, Unit] = {
    case GetRealmlist => sender() ! eventRealmlist
  }
}

object AuthServer {
  def props: Props = Props(classOf[AuthServer])

  val PreferredName = "AuthServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
  val LogonProofHandlerPath = s"$ActorPath/${LogonProofHandler.PreferredName}"
  val ReconnectChallengeHandlerPath = s"$ActorPath/${ReconnectChallengeHandler.PreferredName}"
  val ReconnectProofHandlerPath = s"$ActorPath/${ReconnectProofHandler.PreferredName}"
}

