package wow

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import wow.api.WebServer
import wow.auth.AuthServer
import wow.common.database.Database
import wow.realm.RealmServer
import wow.utils.Reflection
import scalikejdbc.{ConnectionPool, DB}

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  private var startTime: Long = _

  def main(args: Array[String]): Unit = {
    Database.configure()

    Reflection.eagerLoadClasses()

    val system = ActorSystem("wow")

    startTime = System.currentTimeMillis()
    system.actorOf(AuthServer.props, AuthServer.PreferredName)
    system.actorOf(RealmServer.props, RealmServer.PreferredName)

    WebServer.startServer("localhost", 8080, ServerSettings(system), system)
    system.terminate()

    ConnectionPool.closeAll()
  }

  def uptimeMillis(): Long = {
    System.currentTimeMillis() - startTime
  }

  val actorPath = "akka://wow/user"
}
