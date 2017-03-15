package ensiwow.realm.handlers

import ensiwow.realm.protocol.{PayloadlessPacketHandler, PayloadlessPacketHandlerFactory, OpCodes}

/**
  * Created by sknz on 3/15/17.
  */
class ReadyForAccountDataTimesHandler extends PayloadlessPacketHandler {
  /**
    * Processes empty payload
    */
  override protected def process: Unit = {

  }
}

object ReadyForAccountDataTimesHandler extends PayloadlessPacketHandlerFactory[ReadyForAccountDataTimesHandler](OpCodes.ReadyForAccountDataTimes)
