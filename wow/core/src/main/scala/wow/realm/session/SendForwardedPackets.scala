package wow.realm.session

import wow.realm.protocol.PacketSerialization

/**
  * Created by sknz on 5/12/17.
  */
trait SendForwardedPackets {
  this: NetworkWorker =>

  override def receive: Receive = {
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
