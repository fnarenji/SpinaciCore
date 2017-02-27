package ensiwow

import akka.actor.ActorSystem
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
  }

  val actorPath = "akka://EnsiWoW/user"
}
