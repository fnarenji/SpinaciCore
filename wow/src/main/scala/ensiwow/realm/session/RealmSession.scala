package ensiwow.realm.session

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.common.network.Session

import scala.language.postfixOps

/**
  * Handles a realm session
  */
class RealmSession extends Actor with ActorLogging {
  override def receive = PartialFunction.empty
}

object RealmSession extends Session {
  override def props: Props = Props(classOf[RealmSession])

  override def PreferredName = "RealmSession"
}
