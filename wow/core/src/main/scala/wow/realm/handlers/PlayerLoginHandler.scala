package wow.realm.handlers

import wow.realm.protocol.payloads.ClientPlayerLogin
import wow.realm.protocol.{PayloadHandler, PayloadHandlerFactory}
import wow.realm.session.Session

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
