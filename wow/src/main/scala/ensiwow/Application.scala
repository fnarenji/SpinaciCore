package ensiwow

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import ensiwow.api.WebServer
import ensiwow.auth.AuthServer
import ensiwow.realm.RealmServer

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("EnsiWoW")

    system.actorOf(AuthServer.props, AuthServer.PreferredName)
    system.actorOf(RealmServer.props, RealmServer.PreferredName)

    WebServer.startServer("localhost", 8080, ServerSettings(system), system)
    system.terminate()
  }

  val actorPath = "akka://EnsiWoW/user"
}
