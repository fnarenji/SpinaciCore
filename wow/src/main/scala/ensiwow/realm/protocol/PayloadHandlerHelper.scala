package ensiwow.realm.protocol

import akka.actor.ActorContext
import ensiwow.utils.Reflection

object PayloadHandlerHelper {
  /**
    * List of payload handlers companions
    */
  private val payloadHandlersFactories = Reflection.objectsOf[PayloadHandlerFactory[_]]

  /**
    * List of opcodes for which a handler exists
    */
  private val handledOpCodes = payloadHandlersFactories.map(_.opCode)

  /**
    * Handler actor's preferred name
    *
    * @param opCode opcode
    * @return preferred name
    */
  def PreferredName(opCode: OpCodes.Value) = s"${opCode}Handler"

  /**
    * Spawn all actor for handlers
    *
    * @param context actor context
    */
  def spawnActors(context: ActorContext): Unit = {
    payloadHandlersFactories.foreach(factory => context.actorOf(factory.props, PreferredName(factory.opCode)))
  }

  /**
    * Indicates if opcode is handled
    *
    * @param opCode opcode
    * @return true if handled, false otherwise
    */
  def isHandled(opCode: OpCodes.Value): Boolean = handledOpCodes.contains(opCode)
}

