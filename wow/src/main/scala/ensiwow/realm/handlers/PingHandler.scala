package ensiwow.realm.handlers

import ensiwow.realm.protocol.payloads.{ClientPing, ServerPong}
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import ensiwow.realm.session.NetworkWorker

/**
  * Handles ping packets
  */
class PingHandler extends PayloadHandler[ClientPing] {
  override def process(payload: ClientPing): Unit = {
    sender ! NetworkWorker.EventOutgoing(ServerPong(payload.ping))
  }
}

object PingHandler extends PayloadHandlerFactory[PingHandler, ClientPing]
