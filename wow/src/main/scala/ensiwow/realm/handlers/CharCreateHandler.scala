package ensiwow.realm.handlers

import ensiwow.realm.protocol.{PayloadHandler, PayloadHandlerFactory, ResponseCodes}
import ensiwow.realm.protocol.payloads.{ClientCharCreate, CreateInfo, ServerCharCreate}
import ensiwow.realm.session.NetworkWorker

class CharCreateHandler extends PayloadHandler[ClientCharCreate] {

  def validateName(name: String) = {
    log.debug(name)
    if (name.isEmpty) {
      ResponseCodes.CharNameNoName
    } else if (name.length >= CreateInfo.MaxNameLength) {
      ResponseCodes.CharNameFailure
    } else {
      ResponseCodes.CharNameSuccess
    }
  }

  override def process(payload: ClientCharCreate): Unit = {

    // TODO: If CharNameSuccess => store payload.charInfo in the DB
    val response = ServerCharCreate(validateName(payload.createInfo.charInfo.name))
    sender ! NetworkWorker.EventOutgoing(response)
  }
}

object CharCreateHandler extends PayloadHandlerFactory[CharCreateHandler, ClientCharCreate]
