package ensiwow.realm.session

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.common.network.{EventPacket, Session}

import scala.language.postfixOps

/**
  * Handles a realm session
  */
class RealmSession extends Actor with ActorLogging {
  override def receive = {
    case EventPacket(bits) =>
      log.debug(s"Got packet: ${bits.toHex}")
  }
}

object RealmSession extends Session {
  override def props: Props = Props(classOf[RealmSession])

  override def PreferredName = "RealmSession"
}
