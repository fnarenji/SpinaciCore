package ensiwow.realm.handlers

import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import ensiwow.realm.protocol.packets.{ClientPingPacket, ServerPongPacket}
import ensiwow.realm.session.EventTerminateWithPayload

/**
  * Handles ping packets
  */
class PingHandler extends PayloadHandler[ClientPingPacket] {
  override def process(payload: ClientPingPacket): Unit = {
    sender ! EventTerminateWithPayload(ServerPongPacket(payload.ping))
  }
}

object PingHandler extends PayloadHandlerFactory[PingHandler, ClientPingPacket]
