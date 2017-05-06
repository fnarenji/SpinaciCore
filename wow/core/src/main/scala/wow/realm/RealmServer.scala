package wow.realm

import akka.actor.{Actor, ActorLogging, Props}
import wow.Application
import wow.common.VersionInfo
import wow.common.database.{Database, DatabaseConfiguration, Databases}
import wow.common.network.TCPServer
import wow.realm.protocol.{OpCodes, PacketHandlerHelper}
import wow.realm.session.NetworkWorker
import wow.realm.world.WorldState
import scalikejdbc.ConnectionPool

/**
  * RealmServer is the base actor for all services provided by the realm server.
  */
class RealmServer(id: Int) extends Actor with ActorLogging {
  val config = Application.configuration.realms(id)

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  Databases.addRealmServer(id)

  private val dbConfig = config.database
  ConnectionPool.add(Databases.RealmServer(id), dbConfig.connection, dbConfig.username, dbConfig.password)

  PacketHandlerHelper.spawnActors(this)
  context.actorOf(TCPServer.props(NetworkWorker, config.host, config.port), TCPServer.PreferredName)
  context.actorOf(WorldState.props, WorldState.PreferredName)

  override def receive: Receive = PartialFunction.empty
}

object RealmServer {
  def props(id: Int): Props = Props(classOf[RealmServer], id)

  val PreferredName = "RealmServer"
  val ActorPath = s"${Application.ActorPath}/$PreferredName"
  val WorldStatePath = s"$ActorPath/${WorldState.PreferredName}"

  def handlerPath(opCode: OpCodes.Value) = s"$ActorPath/${PacketHandlerHelper.PreferredName(opCode)}"
}

case class RealmServerConfiguration(host: String, port: Int, database: DatabaseConfiguration)

