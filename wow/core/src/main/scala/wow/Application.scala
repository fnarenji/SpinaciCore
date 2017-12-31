package wow

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, SupervisorStrategy}
import akka.http.scaladsl.settings.ServerSettings
import pureconfig._
import scalikejdbc.ConnectionPool
import wow.api.WebServer
import wow.auth.{AccountsState, AuthServer}
import wow.common.config.deriveIntMap
import wow.common.database.Database
import wow.realm.RealmServer
import wow.utils.Reflection

class Application extends Actor with ActorLogging {
  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  Reflection.eagerLoadClasses()

  Database.configure()

  // This database access is required by both authserver and realmserver
  // Can't rely on AuthServer actor existing for it to be initialized
  AuthServer.initializeDatabase()

  context.actorOf(AccountsState.props, AccountsState.PreferredName)
  context.actorOf(AuthServer.props, AuthServer.PreferredName)
  for (id <- Application.configuration.realms.keys) {
    context.actorOf(RealmServer.props(id), RealmServer.PreferredName(id))
  }

  override def postStop(): Unit = {
    // In case any latent connections remain, close them
    // Should not be useful, as actors would close their own connections
    ConnectionPool.closeAll()

    super.postStop()
  }

  override def receive: Receive = PartialFunction.empty
}

object Application {
  private var startTime: Long = _

  val configuration: ApplicationConfiguration = loadConfigOrThrow[ApplicationConfiguration]("wow")

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("wow")
    system.actorOf(Props(new Application), "app")

    startTime = System.currentTimeMillis()
    WebServer.startServer(configuration.webServer.host, configuration.webServer.port, ServerSettings(system), system)

    system.terminate()
  }

  def uptimeMillis(): Long = {
    System.currentTimeMillis() - startTime
  }

  val ActorPath = "akka://wow/user/app/"
}

