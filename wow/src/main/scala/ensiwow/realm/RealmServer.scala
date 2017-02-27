package ensiwow.realm

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.auth.protocol.VersionInfo
import ensiwow.common.network.TCPServer
import ensiwow.realm.session.RealmSession

/**
  * RealmServer is the base actor for all services provided by the realm server.
  */
class RealmServer extends Actor with ActorLogging {
  val address = "127.0.0.1"
  val port = 8085

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  context.actorOf(TCPServer.props(RealmSession, address, port), TCPServer.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object RealmServer {
  def props: Props = Props(classOf[RealmServer])

  val PreferredName = "RealmServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
}
