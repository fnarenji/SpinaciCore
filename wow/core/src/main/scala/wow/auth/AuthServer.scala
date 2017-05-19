package wow.auth

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import scalikejdbc._
import wow.Application
import wow.auth.AuthServer.{NotifyRealmOnline, UpdatePopulation}
import wow.auth.protocol.RealmFlags
import wow.auth.session.AuthSession
import wow.common.VersionInfo
import wow.common.database.{DatabaseConfiguration, Databases, _}
import wow.common.network.TCPServer
import wow.realm.RealmServer.SendPopulation
import wow.utils.AutoRestartSupervisor

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * AuthServer is the base actor for all services provided by the authentication server.
  *
  * This actor is unique (e.g. singleton) per ActorSystem.
  */
class AuthServer extends Actor with ActorLogging {
  private val config = Application.configuration.auth

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  initializeDatabase()

  context.actorOf(TCPServer.props(AuthSession, config.host, config.port), TCPServer.PreferredName)

  private case object RequestPopulationUpdate

  context.system.scheduler.schedule(Duration.Zero, 5 seconds, self, RequestPopulationUpdate)(context.dispatcher)

  private val realmsByActor = mutable.HashMap[ActorRef, Int]()

  import AuthServer.realms

  override def receive: Receive = {
    case NotifyRealmOnline(id) =>
      realms(id).flags = realms(id).flags - RealmFlags.Offline

      realmsByActor(sender) = id
      context.watch(sender)

    case Terminated(sender) =>
      val id = realmsByActor(sender)
      realms(id).flags = realms(id).flags + RealmFlags.Offline

    case RequestPopulationUpdate =>
      realmsByActor.keys.foreach(realm => realm ! SendPopulation)

    case UpdatePopulation(population) =>
      val id = realmsByActor(sender)
      val realm = realms(id)

      realm.population = population
  }

  override def postStop(): Unit = {
    ConnectionPool.close(AuthDB)

    super.postStop()
  }

  /**
    * Creates connection to database and sets it up if necessary
    */
  private def initializeDatabase(): Unit = {
    val dbConfig = config.database

    DatabaseHelpers.migrate("auth", dbConfig)
    DatabaseHelpers.connect(Databases.AuthServer, dbConfig)
  }
}

object AuthServer {
  private val PreferredNameChild = "authserver"

  def props: Props = Props(new AutoRestartSupervisor(Props(classOf[AuthServer]), PreferredNameChild))

  val PreferredName = "authsuperv"
  val ActorPath = s"${Application.ActorPath}/$PreferredName/$PreferredNameChild"

  case class NotifyRealmOnline(id: Int)

  case class UpdatePopulation(population: Float)

  val realms: Map[Int, RealmInfo] = {
    for ((id, realmConfig) <- Application.configuration.realms) yield {
      (id, RealmInfo(id, realmConfig))
    }
  }
}

case class AuthServerConfiguration(host: String, port: Int, database: DatabaseConfiguration)

