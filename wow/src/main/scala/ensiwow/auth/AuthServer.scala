package ensiwow.auth

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.crypto.Srp6Protocol
import ensiwow.auth.handlers.{LogonChallengeHandler, LogonProofHandler}
import ensiwow.auth.network.TCPServer
import ensiwow.auth.protocol.VersionInfo

/**
  * AuthServer is the base actor for all services provided by the authentication server.
  *
  * This actor is unique (e.g. singleton) per ActorSystem.
  * It holds ownership of the stateless packet handlers.
  */
class AuthServer extends Actor with ActorLogging {
  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  // TODO: handlers should be put in a pool so that they scale according to the load
  context.actorOf(LogonChallengeHandler.props, LogonChallengeHandler.PreferredName)
  context.actorOf(LogonProofHandler.props, LogonProofHandler.PreferredName)
  context.actorOf(TCPServer.props, TCPServer.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object AuthServer {
  def props: Props = Props(new AuthServer)

  val PreferredName = "AuthServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
  val LogonProofHandlerPath = s"$ActorPath/${LogonProofHandler.PreferredName}"
}
