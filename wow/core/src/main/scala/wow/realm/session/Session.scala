package wow.realm.session

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import wow.auth.AccountsState
import wow.auth.AccountsState.NotifyAccountOnline
import wow.realm.entities.Guid
import wow.realm.protocol._
import wow.realm.session.Session.CreatePlayer
import wow.realm.{RealmContext, RealmContextData}

/**
  * Represents a realm session
  */
class Session(val login: String, override val networkWorker: ActorRef)(override implicit val realm: RealmContextData)
  extends Actor
          with ActorLogging
          with RealmContext
          with PacketHandlerTag
          with ForwardToNetworkWorker {
  var player: Option[ActorRef] = _

  context.actorSelection(AccountsState.ActorPath) ! NotifyAccountOnline(login, networkWorker)

  override def receive: Receive = {
    case CreatePlayer(guid: Guid) =>
      val ref = context.actorOf(SessionPlayer.props(guid, networkWorker), SessionPlayer.PreferredName(guid))
      player = Some(ref)
      sender() ! ref
      context.watch(ref)

    case NetworkWorker.HandlePacket(header, payloadBits) =>
      PacketHandler(header, payloadBits)(this)

    case Terminated(subject) if subject == player.getOrElse(ActorRef.noSender) =>
      // Bring player back to character screen is its player actor crashes
      sendOpCode(OpCodes.SLogoutComplete)
  }
}

object Session {
  def props(login: String, networkWorker: ActorRef)(implicit realm: RealmContextData): Props =
    Props(new Session(login, networkWorker))

  def PreferredName(login: String) = s"session-$login"

  case class CreatePlayer(guid: Guid)

}

