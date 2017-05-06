package wow.realm.handlers

import wow.realm.protocol.{PacketHandler, PacketHandlerTag}
import wow.realm.session.{NetworkWorker, Session, SessionPlayer}

/**
  * Enumeration of packet handler type
  */
object HandledBy extends Enumeration {
  val NetworkWorker = Value
  val Session = Value
  val Player = Value
  val Unhandled = Value

  import scala.reflect.runtime.universe._

  /**
    * Maps a type tag to to corresponding enum value
    */
  val TypeTagMap: Map[TypeTag[PacketHandler[PacketHandlerTag]], HandledBy.Value] = {
    /**
      * Get type tag of generic type A, cast as PacketHandlerTag super type
      */
    def handlerTag[A <: PacketHandlerTag](implicit typeTag: TypeTag[PacketHandler[A]]) =
      typeTag.asInstanceOf[TypeTag[PacketHandler[PacketHandlerTag]]]

    Map(
      handlerTag[NetworkWorker] -> NetworkWorker,
      handlerTag[Session] -> Session,
      handlerTag[SessionPlayer] -> Player
    )
  }
}
