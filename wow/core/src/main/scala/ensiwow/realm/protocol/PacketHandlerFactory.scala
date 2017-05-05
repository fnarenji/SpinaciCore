package ensiwow.realm.protocol

import akka.actor.Props

import scala.reflect.ClassTag

/**
  * Packet handler factory
  *
  * @tparam PacketHandler packet handler
  */
abstract class PacketHandlerFactory[PacketHandler <: PacketHandler : ClassTag] {
  private val clazz = implicitly[ClassTag[PacketHandler]].runtimeClass

  final def props: Props = Props(clazz)

  final def preferredName: String = clazz.getSimpleName

  def opCodes: OpCodes.ValueSet
}

/**
  * Payload handler factory
  *
  * @tparam PayloadHandler payload handler type
  * @tparam Payload        payload type
  */
abstract class PayloadHandlerFactory[PayloadHandler <: PayloadHandler[Payload] : ClassTag,
Payload <: Payload with ClientSide : OpCodeProvider] extends PacketHandlerFactory[PayloadHandler] {
  private val opCode = implicitly[OpCodeProvider[Payload]].opCode

  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(opCode)
}

/**
  * Multi op code payload handler factory
  *
  * @tparam PayloadHandler payload handler type
  * @tparam Payload        payload type
  */
abstract class MultiPayloadHandlerFactory[PayloadHandler <: PayloadHandler[Payload] : ClassTag,
Payload <: Payload with ClientSide](handledOpCodes: OpCodes.Value*) extends PacketHandlerFactory[PayloadHandler] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes: _*)
}

/**
  * Payloadless handler factory
  *
  * @param handledOpCodes handled opcodes
  * @tparam PayloadlessPacketHandler handler type
  */
abstract class PayloadlessPacketHandlerFactory[PayloadlessPacketHandler <: PayloadlessPacketHandler : ClassTag]
(handledOpCodes: OpCodes.Value*) extends PacketHandlerFactory[PayloadlessPacketHandler] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes: _*)
}

