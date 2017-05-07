package wow.realm.handlers

import wow.realm.protocol.payloads.{ClientPing, ServerPong}
import wow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import wow.realm.session.NetworkWorker

/**
  * Handles ping packets
  */
class PingHandler extends PayloadHandler[ClientPing] {
  override def process(payload: ClientPing): Unit = {
    sender ! NetworkWorker.EventOutgoing(ServerPong(payload.ping))
  }
}

object PingHandler extends PayloadHandlerFactory[PingHandler, ClientPing]
