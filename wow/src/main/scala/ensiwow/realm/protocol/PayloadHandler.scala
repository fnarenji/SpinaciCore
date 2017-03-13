package ensiwow.realm.protocol

import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import ensiwow.realm.session.EventHandlerFailure
import ensiwow.utils.Reflection
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

import scala.reflect.ClassTag

/**
  * Incoming payload bits
  *
  * @param bits bits of payload
  */
case class EventPayload(bits: BitVector)

/**
  * Payload handler base class
  *
  * @param codec codec used for payload serialization
  * @tparam A payload type
  */
abstract class PayloadHandler[A <: Payload[ClientHeader]](implicit codec: Codec[A]) extends Actor with ActorLogging {
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

/**
  * Payload handler companion object type
  * @param opCodeProvider op code provider for payload
  * @param classTag class tag for payload handler
  * @tparam TPayloadHandler payload handler
  * @tparam TPayload payload
  */
abstract class PayloadHandlerCompanion[TPayloadHandler <: PayloadHandler[TPayload], TPayload <: Payload[ClientHeader]]
(implicit opCodeProvider: OpCodeProvider[TPayload], classTag: ClassTag[TPayloadHandler]) {
  def props: Props = Props(classTag.runtimeClass)

  final val OpCode: OpCodes.Value = opCodeProvider.opCode

  final val PreferredName = PayloadHandlerHelper.PreferredName(OpCode)
}

object PayloadHandlerHelper {
  /**
    * List of payload handlers companions
    */
  private val payloadHandlersCompanions = Reflection.objectsOf[PayloadHandlerCompanion[_, _]]

  /**
    * List of opcodes for which a handler exists
    */
  private val handledOpCodes = payloadHandlersCompanions.map(_.OpCode)

  /**
    * Handler actor's preferred name
    * @param opCode opcode
    * @return preferred name
    */
  def PreferredName(opCode: OpCodes.Value) = s"${opCode}Handler"

  /**
    * Spawn all actor for handlers
    * @param context actor context
    */
  def spawnActors(context: ActorContext): Unit = {
    payloadHandlersCompanions.foreach(comp => context.actorOf(comp.props, comp.PreferredName))
  }

  /**
    * Indicates if opcode is handled
    * @param opCode opcode
    * @return true if handled, false otherwise
    */
  def isHandled(opCode: OpCodes.Value): Boolean = handledOpCodes.contains(opCode)
}
