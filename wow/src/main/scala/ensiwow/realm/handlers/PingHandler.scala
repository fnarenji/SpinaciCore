package ensiwow.realm.handlers

import ensiwow.realm.protocol.payloads.{ClientPing, ServerPong}
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import ensiwow.realm.session.EventTerminateWithPayload

/**
  * Handles ping packets
  */
class PingHandler extends PayloadHandler[ClientPing] {
  override def process(payload: ClientPing): Unit = {
    sender ! EventTerminateWithPayload(ServerPong(payload.ping))
  }
}

object PingHandler extends PayloadHandlerFactory[PingHandler, ClientPing]
