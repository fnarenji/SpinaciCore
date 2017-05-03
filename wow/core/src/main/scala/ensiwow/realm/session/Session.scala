package ensiwow.realm.session

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.realm.entities.Guid
import ensiwow.realm.session.Session.PlayerLogin

/**
  * Represents a realm session
  */
class Session extends Actor with ActorLogging {
  private val networkWorker = context.parent

  override def receive: Receive = {
    case PlayerLogin(guid) =>
      context.actorOf(SessionPlayer.props(guid, networkWorker), SessionPlayer.PreferredName)
  }
}

object Session {
  def props: Props = Props(classOf[Session])

  val PreferredName = "Session"

  sealed trait Event

  case class PlayerLogin(characterGuid: Guid) extends Event
}
