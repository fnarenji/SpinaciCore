package wow.realm.session

import scodec.Codec
import scodec.bits.BitVector
import wow.realm.protocol._

import scala.concurrent.duration._

/**
  * Sends TCP packets to client
  */
private[session] trait SendPacketsToClient extends CanSendPackets {
  this: NetworkWorker =>
  private val terminationDelay = 1 second

  override def terminateDelayed(): Unit = {
    context.system.scheduler.scheduleOnce(terminationDelay)(terminateNow())(context.dispatcher)
  }

  override def terminateNow(): Unit = {
    disconnect()
    context.stop(self)
  }

  override def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit = {
    log.debug("Sending " + payload)
    val bits = PacketSerialization.outgoing(payload)(sessionCipher)

    sendRaw(bits)
  }

  override def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit = {
    val bits = PacketSerialization.outgoing(payloadBits, opCode)(sessionCipher)

    sendRaw(bits)
  }

  override def sendRaw(headerBits: BitVector, payloadBits: BitVector): Unit = {
    val bits = PacketSerialization.outgoing(headerBits, payloadBits)(sessionCipher)

    sendRaw(bits)
  }

  override def sendRaw(bits: BitVector): Unit = outgoing(bits)
}
