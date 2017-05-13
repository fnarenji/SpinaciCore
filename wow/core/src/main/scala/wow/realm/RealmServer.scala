package wow.realm

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.EventStream
import org.flywaydb.core.Flyway
import scalikejdbc.ConnectionPool
import wow.Application
import wow.auth.AuthServer
import wow.common.VersionInfo
import wow.common.database._
import wow.common.network.TCPServer
import wow.realm.RealmServer.CreateSession
import wow.realm.session.{NetworkWorkerFactory, Session}
import wow.realm.world.WorldState

case class RealmContextData(id: Int, eventStream: EventStream, serverRef: ActorRef)

trait RealmContext {
  implicit val realm: RealmContextData
}

/**
  * RealmServer is the base actor for all services provided by the realm server.
  */
class RealmServer(id: Int) extends Actor with ActorLogging with RealmContext {
  override implicit lazy val realm: RealmContextData = RealmContextData(
    id,
    new EventStream(context.system, context.system.settings.DebugEventStream),
    self
  )

  private val config = Application.configuration.realms(realm.id)
  private val authServer = context.actorSelection(AuthServer.ActorPath)

  log.info(s"${config.name} startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  initializeDatabase()

  authServer ! AuthServer.RegisterRealm(realm.id, config.name, config.host, config.port)
  context.actorOf(TCPServer.props(new NetworkWorkerFactory, config.host, config.port), TCPServer.PreferredName)
  context.actorOf(WorldState.props, WorldState.PreferredName)

  override def receive: Receive = {
    case CreateSession(login, networkWorker) =>
      log.debug(s"Create session for $login")
      val ref = context.actorOf(Session.props(login, networkWorker))
      sender() ! ref
  }

  override def postStop(): Unit = {
    ConnectionPool.close(RealmDB)

    super.postStop()
  }

  /**
    * Creates connection to database and sets it up if necessary
    */
  private def initializeDatabase(): Unit = {
    val dbConfig = config.database

    migrateDatabase()

    Databases.registerRealm(realm.id)
    ConnectionPool.add(RealmDB, dbConfig.connection, dbConfig.username, dbConfig.password)

    def migrateDatabase() = {
      val migration = new Flyway()

      migration.setDataSource(dbConfig.connection, dbConfig.username, dbConfig.password)
      migration.setLocations("classpath:db/realm")
      migration.baseline()
      migration.migrate()
      migration.validate()
    }
  }
}

object RealmServer {
  def props(realmId: Int) = Props(new RealmServer(realmId))

  def PreferredName(id: Int) = s"realm-$id"

  def ActorPath(id: Int) = s"${Application.ActorPath}/${PreferredName(id)}"

  val WorldStatePath = s"${ActorPath(1)}/${WorldState.PreferredName}"

  case class CreateSession(login: String, networkWorker: ActorRef)

}

case class RealmServerConfiguration(name: String, host: String, port: Int, database: DatabaseConfiguration)

