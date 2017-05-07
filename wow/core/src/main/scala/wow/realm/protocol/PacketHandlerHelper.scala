package wow.realm.protocol

import akka.actor.{Actor, ActorLogging}
import wow.utils.Reflection

import scala.language.postfixOps

object PacketHandlerHelper {
  /**
    * List of packet handlers companions
    */
  private val packetHandlerFactories = Reflection.objectsOf[PacketHandlerFactory[_ <: PacketHandler]]

  /**
    * List of opcodes for which a handler exists
    */
  private val handledOpCodes: Map[OpCodes.Value, PacketHandlerFactory[_ <: PacketHandler]] = packetHandlerFactories.flatMap(
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
  def PreferredName(opCode: OpCodes.Value): String = handledOpCodes(opCode).preferredName

  /**
    * Spawn all actor for handlers
    */
  def spawnActors(self: Actor with ActorLogging): Unit = {
    for (factory <- packetHandlerFactories) {
      self.context.actorOf(factory.props, factory.preferredName)
    }
    self.log.debug(s"Spawned actors for: {}", handledOpCodes.keys.map(_.toString).reduceLeft[String](_ + ", " + _))
  }

  /**
    * Indicates if opcode is handled
    *
    * @param opCode opcode
    * @return true if handled, false otherwise
    */
  def isHandled(opCode: OpCodes.Value): Boolean = handledOpCodes.contains(opCode)
}

