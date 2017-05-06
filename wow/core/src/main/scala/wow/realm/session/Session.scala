package wow.realm.session

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scodec.Codec
import scodec.bits.BitVector
import wow.realm.entities.Guid
import wow.realm.protocol.{OpCodes, _}
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

  override def receive: Receive = {
    case CreatePlayer(guid: Guid) =>
      val ref = context.actorOf(SessionPlayer.props(guid, networkWorker))
      player = Some(ref)
      sender() ! ref

    case NetworkWorker.HandlePacket(header, payloadBits) =>
      PacketHandler(header, payloadBits)(this)
  }
}

object Session {
  def props(login: String, networkWorker: ActorRef)(implicit realm: RealmContextData): Props =
    Props(new Session(login, networkWorker))

  def PreferredName(login: String) = s"session-$login"

  case class CreatePlayer(guid: Guid)
}

