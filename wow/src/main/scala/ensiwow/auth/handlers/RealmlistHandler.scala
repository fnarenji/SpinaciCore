package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import ensiwow.auth.GetRealmlist
import ensiwow.auth.protocol.packets.{ClientRealmlistPacket, ServerRealmlistPacket}
import ensiwow.auth.session.{EventRealmlist, EventRealmlistFailure, EventRealmlistSuccess}

import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

case class RealmlistPacket(packet: ClientRealmlistPacket)

/**
  * Handles realm list requests
  */
class RealmlistHandler extends Actor with ActorLogging {
  implicit val timeout = Timeout(2 second)
  import context.dispatcher
  override def receive: PartialFunction[Any, Unit] = {
    case RealmlistPacket(_) =>
      val origSender = sender

      val futureServerRealmlist: Future[ServerRealmlistPacket] = (context.parent ? GetRealmlist).mapTo[ServerRealmlistPacket]

      val futureEvent: Future[EventRealmlist] = {
        futureServerRealmlist map { realms =>
          EventRealmlistSuccess(realms)
        } recover {
          case _ => EventRealmlistFailure()
        }
      }

      // TODO: Could be wrote more elegantly
      // Maybe manage the future's completion in AuthSession ...
      futureEvent onComplete {
        case Success(event) => origSender ! event
        case Failure(event) => origSender ! event
      }
  }
}

object RealmlistHandler {
  val PreferredName = "RealmlistHandler"

  def props = Props(new RealmlistHandler)
}
