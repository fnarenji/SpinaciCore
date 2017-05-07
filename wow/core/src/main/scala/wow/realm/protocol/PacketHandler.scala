package wow.realm.protocol

import akka.actor.{Actor, ActorLogging}
import wow.realm.session.NetworkWorker
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

/**
  * Incoming payload bits
  *
  * @param payloadBits bits of payload
  */
case class EventPacket(header: ClientHeader, payloadBits: BitVector)

/**
  * A packet handler is an actor which handles one type of packet
  */
trait PacketHandler extends Actor with ActorLogging

/**
  * Payload containing packet handler
  *
  * @param codec codec used for payload serialization
  * @tparam A payload type
  */
abstract class PayloadHandler[A <: Payload with ClientSide](implicit codec: Codec[A]) extends PacketHandler {
  private var _currentOpCode: OpCodes.Value = _

  protected def currentOpCode: OpCodes.Value = _currentOpCode

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  protected def process(payload: A): Unit

  override def receive: Receive = {
    case EventPacket(header, bits) =>
      codec.decode(bits) match {
        case Successful(DecodeResult(payload, BitVector.empty)) =>
          _currentOpCode = header.opCode

          process(payload)
        case Failure(err) =>
          sender ! NetworkWorker.EventHandlerFailure(err)
      }
  }
}

/**
  * Payload-less packet handler
  */
abstract class PayloadlessPacketHandler extends PacketHandler {
  private var _currentOpCode: OpCodes.Value = _

  protected def currentOpCode: OpCodes.Value = _currentOpCode

  /**
    * Processes packet without payload
    */
  protected def process(): Unit

  override def receive: Receive = {
    case EventPacket(header, bits) =>
      log.debug(s"processing payloadless packet (payload bits: $bits)")

      _currentOpCode = header.opCode

      process()
  }
}

