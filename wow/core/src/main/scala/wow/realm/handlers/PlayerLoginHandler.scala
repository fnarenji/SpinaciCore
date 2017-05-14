package wow.realm.handlers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import wow.realm.protocol._
import wow.realm.protocol.payloads.ClientPlayerLogin
import wow.realm.session.{NetworkWorker, Session}
import wow.visualizer.{VisualMailboxMetric, VisualizerAPI}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Player login packet handler
  */
object PlayerLoginHandler extends PayloadHandler[NetworkWorker, ClientPlayerLogin] {
  override protected def handle(header: ClientHeader, payload: ClientPlayerLogin)(self: NetworkWorker): Unit = {
    import self._

    implicit val timeout = Timeout(5 seconds)

    // Create player actor
    // Since after this handler, we can receive packets that need to be handled by the player,
    // we must wait to get the player actor reference so that we're certain to have it for next packet received
    val msg = Session.CreatePlayer(payload.guid)
    VisualizerAPI.msg += VisualMailboxMetric(context.self.path.toStringWithoutAddress, session.path.toStringWithoutAddress, msg.hashCode(), msg.toString)
    val getSessionActor = (session ? msg).mapTo[ActorRef]

    player = Await.result(getSessionActor, 5 seconds)
  }
}
