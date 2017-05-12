package wow

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import pureconfig._
import scalikejdbc.ConnectionPool
import wow.api.WebServer
import wow.auth.AuthServer
import wow.common.config.deriveIntMap
import wow.common.database.Database
import wow.realm.RealmServer
import wow.utils.Reflection

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  private var startTime: Long = _

  val configuration: ApplicationConfiguration = loadConfigOrThrow[ApplicationConfiguration]("wow")

  def main(args: Array[String]): Unit = {
    Reflection.eagerLoadClasses()

    Database.configure()

    val system = ActorSystem("wow")

    startTime = System.currentTimeMillis()

    system.actorOf(AuthServer.props, AuthServer.PreferredName)
    for (id <- configuration.realms.keys) {
      system.actorOf(RealmServer.props(id), RealmServer.PreferredName(id))
    }

    WebServer.startServer(configuration.webServer.host, configuration.webServer.port, ServerSettings(system), system)

    system.terminate()

    // In case any latent connections remain, close them
    // Should not be useful, as actors would close their own connections
    ConnectionPool.closeAll()
  }

  def uptimeMillis(): Long = {
    System.currentTimeMillis() - startTime
  }

  val ActorPath = "akka://wow/user"
}

