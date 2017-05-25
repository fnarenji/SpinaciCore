package wow.realm.session

import akka.actor.ActorRef
import scodec.Codec
import scodec.bits.BitVector
import wow.realm.protocol._
import wow.realm.session.network.NetworkWorker

/**
  * Indicates that class can send packets by forwarding them to a NetworkWorker (e.g. Session/SessionPlayer)
  */
private[session] trait ForwardToNetworkWorker extends CanSendPackets {
  /**
    * Network worker actor reference
    */
  val networkWorker: ActorRef

  override def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit = {
    log.debug(s"Sending payload $payload")

    val (headerBits, payloadBits) = PacketSerialization.outgoingSplit(payload, opCodeProvider.opCode)

    sendRaw(headerBits, payloadBits)
  }

  override def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit = {
    networkWorker ! NetworkWorker.SendRawPayload(payloadBits, opCode)
  }

  override def sendRaw(headerBits: BitVector, payloadBits: BitVector): Unit = {
    networkWorker ! NetworkWorker.SendRawSplit(headerBits, payloadBits)
  }

  override def sendRaw(bits: BitVector): Unit = {
    networkWorker ! NetworkWorker.SendRaw(bits)
  }

  override def terminateDelayed(): Unit = {
    networkWorker ! NetworkWorker.Terminate(true)
  }

  override def terminateNow(): Unit = {
    networkWorker ! NetworkWorker.Terminate(false)
  }
}
