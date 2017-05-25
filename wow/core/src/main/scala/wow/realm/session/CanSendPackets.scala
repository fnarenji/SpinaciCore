package wow.realm.session

import akka.actor.{Actor, ActorLogging}
import scodec.Codec
import scodec.bits.BitVector
import wow.realm.protocol._

/**
  * Indicates that class can send packets
  */
private[session] trait CanSendPackets extends Actor with ActorLogging {
  /**
    * Sends the opcode without payload to the client
    *
    * @param opCode op code to be sent
    */
  def sendOpCode(opCode: OpCodes.Value): Unit = sendRaw(BitVector.empty, opCode)

  /**
    * Serializes and sends the payload
    *
    * @param payload        payload
    * @param codec          codec for payload
    * @param opCodeProvider opcode provider for payload
    * @tparam A payload type
    */
  def sendPayload[A <: Payload with ServerSide](payload: A)
    (implicit codec: Codec[A], opCodeProvider: OpCodeProvider[A]): Unit

  /**
    * Sends the payload while prepending it with the header corresponding to opCode.
    * Cipher will be applied to header if applicable.
    *
    * @param payloadBits payload bits
    * @param opCode      opcode
    */
  def sendRaw(payloadBits: BitVector, opCode: OpCodes.Value): Unit

  /**
    * Sends the payload while prepending it with the header bits
    * Cipher will be applied to header if applicable.
    *
    * @param payloadBits payload bits
    * @param headerBits  header bits
    */
  def sendRaw(headerBits: BitVector, payloadBits: BitVector): Unit

  /**
    * Send the bits as passed.
    *
    * @param bits bits to be sent
    */
  def sendRaw(bits: BitVector): Unit

  /**
    * Terminates the connection after a fixed delay.
    */
  def terminateDelayed(): Unit

  /**
    * Instantly terminates the connection
    */
  def terminateNow(): Unit
}

