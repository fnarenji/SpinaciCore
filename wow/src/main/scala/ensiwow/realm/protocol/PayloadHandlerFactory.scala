package ensiwow.realm.protocol

import akka.actor.Props

import scala.reflect.ClassTag

abstract class PayloadHandlerFactory[TPayloadHandler <: PayloadHandler : ClassTag] {
  final def props: Props = Props(implicitly[ClassTag[TPayloadHandler]].runtimeClass)

  def opCode: OpCodes.Value
}

/**
  * Payload handler companion object type
  *
  * @tparam TConcretePayloadHandler payload handler
  * @tparam TPayload                payload
  */
abstract class ConcretePayloadHandlerFactory
[TConcretePayloadHandler <: ConcretePayloadHandler[TPayload] : ClassTag, TPayload <: Payload[ClientHeader] : OpCodeProvider]
  extends PayloadHandlerFactory[TConcretePayloadHandler] {
  override final def opCode: OpCodes.Value = implicitly[OpCodeProvider[TPayload]].opCode
}

abstract class EmptyPayloadHandlerFactory[TPayloadHandler <: EmptyPayloadHandler : ClassTag](handledOpCode: OpCodes.Value)
  extends PayloadHandlerFactory[TPayloadHandler] {
  override final def opCode: OpCodes.Value = handledOpCode
}

