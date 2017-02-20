package ensiwow.auth.handlers

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import ensiwow.auth.GetRealmlist
import ensiwow.auth.protocol.packets.{ClientRealmlistPacket, ServerRealmlistPacket}
import ensiwow.auth.session.{EventRealmlistFailure, EventRealmlistSuccess}

import scala.util.{Failure, Success}
import scala.concurrent.Future


case class RealmlistPacket(packet: ClientRealmlistPacket)

/**
  * Handles realm list requests
  */
class RealmlistHandler extends Actor with ActorLogging {
  override def receive: PartialFunction[Any, Unit] = {
    case RealmlistPacket =>
      val event = {

        val futureRealms: Future[ServerRealmlistPacket] =
          (context.parent ? GetRealmlist).mapTo[ServerRealmlistPacket]

        futureRealms onComplete {
          case Success(packet: ServerRealmlistPacket) =>
            EventRealmlistSuccess(packet)

          case Failure(t) => EventRealmlistFailure(t)
        }
      }
      sender() ! event
  }
}

object RealmlistHandler {
  val PreferredName = "RealmlistHandler"

  def props = Props(new RealmlistHandler)
}
