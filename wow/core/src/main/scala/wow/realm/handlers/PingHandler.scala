package wow.realm.handlers

import wow.realm.protocol.payloads.{ClientPing, ServerPong}
import wow.realm.protocol.{PacketHandlerTag, ClientHeader, PayloadHandler}
import wow.realm.session.NetworkWorker

/**
  * Handles ping packets
  */
object PingHandler extends PayloadHandler[NetworkWorker, ClientPing] {
  protected override def handle(header: ClientHeader, payload: ClientPing)(self: NetworkWorker): Unit = {
    self.sendPayload(ServerPong(payload.ping))
  }
}

