package ensiwow.realm

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.common.VersionInfo
import ensiwow.common.network.TCPServer
import ensiwow.realm.protocol.{OpCodes, PayloadHandlerHelper}
import ensiwow.realm.session.NetworkWorker
import ensiwow.realm.world.WorldState

/**
  * RealmServer is the base actor for all services provided by the realm server.
  */
class RealmServer extends Actor with ActorLogging {
  val address = "127.0.0.1"
  val port = 8085

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  PayloadHandlerHelper.spawnActors(context)
  context.actorOf(TCPServer.props(NetworkWorker, address, port), TCPServer.PreferredName)
  context.actorOf(WorldState.props, WorldState.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object RealmServer {
  def props: Props = Props(classOf[RealmServer])

  val PreferredName = "RealmServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val WorldStatePath = s"$ActorPath/${WorldState.PreferredName}"

  def handlerPath(opCode: OpCodes.Value) = s"$ActorPath/${PayloadHandlerHelper.PreferredName(opCode)}"
}
