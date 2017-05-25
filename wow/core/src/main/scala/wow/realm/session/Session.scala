package wow.realm.session

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import wow.auth.AccountsState
import wow.auth.AccountsState.NotifyAccountOnline
import wow.auth.data.Account
import wow.realm.objects.Guid
import wow.realm.objects.characters.CharacterDao
import wow.realm.protocol._
import wow.realm.session.Session.CreatePlayer
import wow.realm.session.network.NetworkWorker
import wow.realm.{RealmContext, RealmContextData}

/**
  * Represents a realm session
  */
class Session(val account: Account, override val networkWorker: ActorRef)(override implicit val realm: RealmContextData)
  extends Actor
          with ActorLogging
          with RealmContext
          with PacketHandlerTag
          with ForwardToNetworkWorker {
  var player: Option[ActorRef] = _

  context.actorSelection(AccountsState.ActorPath) ! NotifyAccountOnline(account.login, networkWorker)

  override def receive: Receive = {
    case CreatePlayer(guid: Guid) =>
      assert(CharacterDao.isOwner(account.id, guid))

      val ref = context.actorOf(Player.props(guid, networkWorker), Player.PreferredName(guid))
      context.watch(ref)
      player = Some(ref)
      sender() ! ref

    case NetworkWorker.HandlePacket(header, payloadBits) =>
      PacketHandler(header, payloadBits)(this)

    case Terminated(subject) if subject == player.getOrElse(
      throw new IllegalStateException("Got player actor death notification without player actor registered")) =>
      // Bring player back to character screen is its player actor crashes
      sendOpCode(OpCodes.SLogoutComplete)
      player = None
  }
}

object Session {
  def props(account: Account, networkWorker: ActorRef)(implicit realm: RealmContextData): Props =
    Props(new Session(account, networkWorker))

  def PreferredName(login: String) = s"session-$login"

  case class CreatePlayer(guid: Guid)

}

