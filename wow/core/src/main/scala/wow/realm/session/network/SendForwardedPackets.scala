package wow.realm.session.network

import wow.realm.protocol.PacketSerialization

/**
  * Sends packets to client that come from other actors (e.g. Session/Player)
  * Applies cipher if applicable.
  */
private[session] trait SendForwardedPackets {
  this: NetworkWorker =>

  val sendForwardedPacketsReceiver: Receive = {
    case NetworkWorker.SendRawPayload(payloadBits, opCode) =>
      sendRaw(payloadBits, opCode)

    case NetworkWorker.SendRaw(bits) =>
      sendRaw(bits)

    case NetworkWorker.SendRawSplit(headerBits, payloadBits) =>
      val bits = PacketSerialization.outgoing(headerBits, payloadBits)(sessionCipher)

      sendRaw(bits)

    case NetworkWorker.Terminate(delayed) =>
      if (delayed) {
        terminateDelayed()
      } else {
        terminateNow()
      }
  }
}
