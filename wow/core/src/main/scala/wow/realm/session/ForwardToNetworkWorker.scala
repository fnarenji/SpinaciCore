package wow.realm.session

import akka.actor.ActorRef
import scodec.Codec
import scodec.bits.BitVector
import wow.realm.protocol.{OpCodeProvider, OpCodes, Payload, ServerSide}

/**
  * Indicates that class can send packets
  */
trait CanSendPackets {
  def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit

  def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit

  def sendRaw(bits: BitVector): Unit

  def terminateDelayed(): Unit

  def terminateNow(): Unit
}

/**
  * Indicates that class can send packets by forwarding them to a NetworkWorker (e.g. Session/SessionPlayer)
  */
trait ForwardToNetworkWorker {
  val networkWorker: ActorRef

  def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit = {
    networkWorker ! NetworkWorker.SendPayload(payload)
  }

  def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit = {
    networkWorker ! NetworkWorker.SendRawPayload(payloadBits, opCode)
  }

  def sendRaw(bits: BitVector): Unit = {
    networkWorker ! NetworkWorker.SendRaw(bits)
  }

  def terminateDelayed(): Unit = {
    networkWorker ! NetworkWorker.Terminate(true)
  }

  def terminateNow(): Unit = {
    networkWorker ! NetworkWorker.Terminate(false)
  }
}

