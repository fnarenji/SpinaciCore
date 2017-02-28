package ensiwow.auth

import akka.actor.ActorSystem
import ensiwow.auth.sql.Postgresql

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("EnsiWoW")

    system.actorOf(AuthServer.props, AuthServer.PreferredName)


    val bd = new Postgresql()
    bd.init()

  }

  val actorPath = "akka://EnsiWoW/user"
}
