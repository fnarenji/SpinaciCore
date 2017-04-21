package ensiwow.realm.protocol

import akka.actor.ActorContext
import ensiwow.utils.Reflection

object PayloadHandlerHelper {
  /**
    * List of payload handlers companions
    */
  private val payloadHandlersFactories = Reflection.objectsOf[PacketHandlerFactory[_ <: PacketHandler]]

  /**
    * List of opcodes for which a handler exists
    */
  private val handledOpCodes: Map[OpCodes.Value, PacketHandlerFactory[_ <: PacketHandler]] = payloadHandlersFactories.flatMap(
    factory => {
      val builder = Map.newBuilder[OpCodes.Value, PacketHandlerFactory[_ <: PacketHandler]]
      for (elem <- factory.opCodes) {
        builder += elem -> factory
      }
      builder.result()
    }) toMap

  /**
    * Handler actor's preferred name
    *
    * @param opCode opcode
    * @return preferred name
    */
  def PreferredName(opCode: OpCodes.Value) = handledOpCodes(opCode).preferredName

  /**
    * Spawn all actor for handlers
    *
    * @param context actor context
    */
  def spawnActors(context: ActorContext): Unit = {
    for (factory <- payloadHandlersFactories) {
      context.actorOf(factory.props, factory.preferredName)
    }
  }

  /**
    * Indicates if opcode is handled
    *
    * @param opCode opcode
    * @return true if handled, false otherwise
    */
  def isHandled(opCode: OpCodes.Value): Boolean = handledOpCodes.contains(opCode)
}

