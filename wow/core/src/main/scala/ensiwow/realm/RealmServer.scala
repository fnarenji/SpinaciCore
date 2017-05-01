package ensiwow.realm

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.common.VersionInfo
import ensiwow.common.database.{Database, Databases}
import ensiwow.common.network.TCPServer
import ensiwow.realm.protocol.{OpCodes, PacketHandlerHelper}
import ensiwow.realm.session.NetworkWorker
import ensiwow.realm.world.WorldState
import scalikejdbc.ConnectionPool

/**
  * RealmServer is the base actor for all services provided by the realm server.
  */
class RealmServer extends Actor with ActorLogging {
  val address = "127.0.0.1"
  val port = 8085

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  Databases.addRealmServer(1)
  ConnectionPool.add(Databases.RealmServer(1), "jdbc:postgresql://localhost:5432/ensiwow_realm", "ensiwow", "")

  PacketHandlerHelper.spawnActors(this)
  context.actorOf(TCPServer.props(NetworkWorker, address, port), TCPServer.PreferredName)
  context.actorOf(WorldState.props, WorldState.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object RealmServer {
  def props: Props = Props(classOf[RealmServer])

  val PreferredName = "RealmServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val WorldStatePath = s"$ActorPath/${WorldState.PreferredName}"

  def handlerPath(opCode: OpCodes.Value) = s"$ActorPath/${PacketHandlerHelper.PreferredName(opCode)}"
}
