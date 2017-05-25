package wow.realm.handlers

import wow.realm.protocol.payloads.{ClientPing, ServerPong}
import wow.realm.protocol.{ClientHeader, PayloadHandler}
import wow.realm.session.network.NetworkWorker

/**
  * Handles ping packets
  */
object PingHandler extends PayloadHandler[NetworkWorker, ClientPing] {
  protected override def handle(header: ClientHeader, payload: ClientPing)(self: NetworkWorker): Unit = {
    import self._

    sendPayload(ServerPong(payload.ping))
  }
}

