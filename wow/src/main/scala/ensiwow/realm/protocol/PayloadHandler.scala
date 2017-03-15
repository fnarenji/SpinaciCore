package ensiwow.realm.protocol

import akka.actor.{Actor, ActorLogging}
import ensiwow.realm.session.{EventEmptyHandlerFailure, EventHandlerFailure}
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

/**
  * Incoming payload bits
  *
  * @param bits bits of payload
  */
case class EventPayload(bits: BitVector)

trait PayloadHandler extends Actor with ActorLogging

/**
  * Payload handler base class
  *
  * @param codec codec used for payload serialization
  * @tparam A payload type
  */
abstract class ConcretePayloadHandler[A <: Payload[ClientHeader]](implicit codec: Codec[A]) extends PayloadHandler {
  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  protected def process(payload: A): Unit

  override def receive: Receive = {
    case EventPayload(bits) =>
      codec.decode(bits) match {
        case Successful(DecodeResult(payload, BitVector.empty)) =>
          process(payload)
        case Failure(err) =>
          sender ! EventHandlerFailure(err)
      }
  }
}

abstract class EmptyPayloadHandler extends PayloadHandler {
  /**
    * Processes empty payload
    */
  protected def process: Unit

  override def receive: Receive = {
    case EventPayload(bits) =>
      bits match {
        case BitVector.empty =>
          process
        case _ =>
          sender ! EventEmptyHandlerFailure
      }
  }
}

