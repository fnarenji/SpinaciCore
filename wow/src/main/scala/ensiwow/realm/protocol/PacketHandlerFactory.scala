package ensiwow.realm.protocol

import akka.actor.Props

import scala.reflect.ClassTag

/**
  * Packet handler factory
  * @tparam TPacketHandler packet handler
  */
abstract class PacketHandlerFactory[TPacketHandler <: PacketHandler : ClassTag] {
  final def props: Props = Props(implicitly[ClassTag[TPacketHandler]].runtimeClass)

  def opCode: OpCodes.Value
}

/**
  * Payload handler factory
  *
  * @tparam TPayloadHandler payload handler type
  * @tparam TPayload                payload type
  */
abstract class PayloadHandlerFactory[TPayloadHandler <: PayloadHandler[TPayload] : ClassTag,
TPayload <: Payload[ClientHeader] : OpCodeProvider] extends PacketHandlerFactory[TPayloadHandler] {
  override final def opCode: OpCodes.Value = implicitly[OpCodeProvider[TPayload]].opCode
}

/**
  * Payloadless handler factory
  * @param handledOpCode handled opcode
  * @tparam TPayloadlessPacketHandler handler type
  */
abstract class PayloadlessPacketHandlerFactory[TPayloadlessPacketHandler <: PayloadlessPacketHandler : ClassTag]
(handledOpCode: OpCodes.Value) extends PacketHandlerFactory[TPayloadlessPacketHandler] {
  override final def opCode: OpCodes.Value = handledOpCode
}

