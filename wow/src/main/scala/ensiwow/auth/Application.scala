package ensiwow.auth

import akka.actor.ActorSystem
import ensiwow.auth.sql.Postgresql
import ensiwow.auth.data.Account

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("EnsiWoW")

    system.actorOf(AuthServer.props, AuthServer.PreferredName)


    val bd = new Postgresql()
    bd.init()

    Account.createAccount("t","t");
  }

  val actorPath = "akka://EnsiWoW/user"
}
