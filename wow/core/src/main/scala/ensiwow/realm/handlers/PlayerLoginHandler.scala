package ensiwow.realm.handlers

import ensiwow.realm.protocol.payloads.ClientPlayerLogin
import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import ensiwow.realm.session.Session

/**
  * Player login packet handler
  */
class PlayerLoginHandler extends PayloadHandler[ClientPlayerLogin] {
  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  override protected def process(payload: ClientPlayerLogin): Unit = {
    val session = context.actorSelection(sender.path.child(Session.PreferredName))
    session ! Session.PlayerLogin(payload.guid)
  }
}

object PlayerLoginHandler extends PayloadHandlerFactory[PlayerLoginHandler, ClientPlayerLogin]
