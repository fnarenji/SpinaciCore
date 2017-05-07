package wow.realm.protocol

import akka.actor.Props

import scala.reflect.ClassTag

/**
  * Packet handler factory
  *
  * @tparam A packet handler
  */
abstract class PacketHandlerFactory[A <: PacketHandler : ClassTag] {
  private val clazz = implicitly[ClassTag[A]].runtimeClass

  final def props: Props = Props(clazz)

  final def preferredName: String = clazz.getSimpleName

  def opCodes: OpCodes.ValueSet
}

/**
  * Payload handler factory
  *
  * @tparam A payload handler type
  * @tparam B payload type
  */
abstract class PayloadHandlerFactory[A <: PayloadHandler[B] : ClassTag, B <: Payload with ClientSide : OpCodeProvider]
  extends PacketHandlerFactory[A] {
  private val opCode = implicitly[OpCodeProvider[B]].opCode

  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(opCode)
}

/**
  * Multi op code payload handler factory
  *
  * @tparam A payload handler type
  * @tparam B payload type
  */
abstract class MultiPayloadHandlerFactory[A <: PayloadHandler[B] : ClassTag, B <: Payload with ClientSide](
  handledOpCodes: OpCodes.Value*) extends PacketHandlerFactory[A] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes: _*)
}

/**
  * Payloadless handler factory
  *
  * @param handledOpCodes handled opcodes
  * @tparam A handler type
  */
abstract class PayloadlessPacketHandlerFactory[A <: PayloadlessPacketHandler : ClassTag](handledOpCodes: OpCodes.Value*)
  extends PacketHandlerFactory[A] {
  override final def opCodes: OpCodes.ValueSet = OpCodes.ValueSet(handledOpCodes: _*)
}

