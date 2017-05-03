package ensiwow.realm.protocol

import akka.actor.Props

import scala.reflect.ClassTag

/**
  * Packet handler factory
  * @tparam TPacketHandler packet handler
  */
abstract class PacketHandlerFactory[TPacketHandler <: PacketHandler : ClassTag] {
  private val clazz = implicitly[ClassTag[TPacketHandler]].runtimeClass

  final def props: Props = Props(clazz)

  final def preferredName: String = clazz.getSimpleName

  def opCodes: OpCodes.ValueSet
}

/**
  * Payload handler factory
  *
  * @tparam TPayloadHandler payload handler type
  * @tparam TPayload                payload type
  */
abstract class PayloadHandlerFactory[TPayloadHandler <: PayloadHandler[TPayload] : ClassTag,
TPayload <: Payload with ClientSide : OpCodeProvider] extends PacketHandlerFactory[TPayloadHandler] {
  private val opCode = implicitly[OpCodeProvider[TPayload]].opCode

  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(opCode)
}

/**
  * Multi op code payload handler factory
  *
  * @tparam TPayloadHandler payload handler type
  * @tparam TPayload                payload type
  */
abstract class MultiPayloadHandlerFactory[TPayloadHandler <: PayloadHandler[TPayload] : ClassTag,
TPayload <: Payload with ClientSide](handledOpCodes: OpCodes.Value*) extends PacketHandlerFactory[TPayloadHandler] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes:_*)
}

/**
  * Payloadless handler factory
  * @param handledOpCodes handled opcodes
  * @tparam TPayloadlessPacketHandler handler type
  */
abstract class PayloadlessPacketHandlerFactory[TPayloadlessPacketHandler <: PayloadlessPacketHandler : ClassTag]
(handledOpCodes: OpCodes.Value*) extends PacketHandlerFactory[TPayloadlessPacketHandler] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes:_*)
}

