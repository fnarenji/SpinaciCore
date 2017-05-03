package ensiwow

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import ensiwow.api.WebServer
import ensiwow.auth.AuthServer
import ensiwow.realm.RealmServer
import ensiwow.realm.handlers.PlayerLoginHandler
import ensiwow.utils.Reflection

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  private var startTime: Long = _

  def main(args: Array[String]): Unit = {
    Reflection.eagerLoadClasses()

    val system = ActorSystem("EnsiWoW")

    startTime = System.currentTimeMillis()
    PlayerLoginHandler.opCodes
    system.actorOf(AuthServer.props, AuthServer.PreferredName)
    system.actorOf(RealmServer.props, RealmServer.PreferredName)

    WebServer.startServer("localhost", 8080, ServerSettings(system), system)
    system.terminate()
  }

  def uptimeMillis(): Long = {
    System.currentTimeMillis() - startTime
  }

  val actorPath = "akka://EnsiWoW/user"
}
