package ensiwow.auth

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.handlers.LogonChallengeHandler
import ensiwow.auth.network.TCPServer
import ensiwow.auth.protocol.VersionInfo

/**
  * Created by sknz on 2/13/17.
  */
class AuthServer extends Actor with ActorLogging {
  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  context.actorOf(LogonChallengeHandler.props, LogonChallengeHandler.PreferredName)
  context.actorOf(TCPServer.props, TCPServer.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object AuthServer {
  def props: Props = Props(new AuthServer)

  val PreferredName = "AuthServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
}
