package ensiwow.realm.session

import akka.actor.{Actor, ActorLogging, Props}

/**
  * Represents a realm session
  */
class Session extends Actor with ActorLogging {

  override def receive: Receive = {
    case _ =>
  }
}

object Session {
  def props: Props = Props(classOf[Session])
  val PreferredName = "Session"
}
