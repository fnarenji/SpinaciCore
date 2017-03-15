package ensiwow.realm.protocol

import akka.actor.{Actor, ActorLogging}
import ensiwow.realm.session.{EventEmptyHandlerFailure, EventHandlerFailure}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

/**
  * Incoming payload bits
  *
  * @param payloadBits bits of payload
  */
case class EventPacket(payloadBits: BitVector)

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
abstract class PayloadHandler[A <: Payload[ClientHeader]](implicit codec: Codec[A]) extends PacketHandler {
  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  protected def process(payload: A): Unit

  override def receive: Receive = {
    case EventPacket(bits) =>
      codec.decode(bits) match {
        case Successful(DecodeResult(payload, BitVector.empty)) =>
          process(payload)
        case Failure(err) =>
          sender ! EventHandlerFailure(err)
      }
  }
}

/**
  * Payload-less packet handler
  */
abstract class PayloadlessPacketHandler extends PacketHandler {
  /**
    * Processes packet without payload
    */
  protected def process: Unit

  override def receive: Receive = {
    case EventPacket(bits) =>
      bits match {
        case BitVector.empty =>
          process
        case _ =>
          sender ! EventEmptyHandlerFailure
      }
  }
}

