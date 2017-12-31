package wow.auth

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import scalikejdbc._
import wow.Application
import wow.auth.AuthServer.{NotifyRealmOnline, UpdatePopulation}
import wow.auth.protocol.RealmFlags
import wow.auth.session.AuthSession
import wow.common.VersionInfo
import wow.common.database._
import wow.common.network.TCPServer
import wow.realm.RealmServer.GetPopulation
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

  context.actorOf(TCPServer.props(AuthSession, config.host, config.port), TCPServer.PreferredName)

  private case object RequestPopulationUpdate

  private val populationUpdateToken =
    context.system.scheduler.schedule(Duration.Zero, 5 minutes, self, RequestPopulationUpdate)(context.dispatcher)

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
      realmsByActor.keys.foreach(_ ! GetPopulation)

    case UpdatePopulation(population) =>
      val id = realmsByActor(sender)
      val realm = realms(id)

      realm.population = population
  }

  override def postStop(): Unit = {
    ConnectionPool.close(AuthDB)
    populationUpdateToken.cancel()

    super.postStop()
  }
}

object AuthServer {
  /**
    * Creates connection to database and sets it up if necessary
    */
  def initializeDatabase(): Unit = {
    val dbConfig = Application.configuration.auth.database

    DatabaseHelpers.migrate("auth", dbConfig, AuthPlaceHolders)
    DatabaseHelpers.connect(Databases.AuthServer, dbConfig)
  }

  private val PreferredNameChild = "authserver"

  def props: Props = Props(new AutoRestartSupervisor(Props(new AuthServer()), PreferredNameChild))

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

