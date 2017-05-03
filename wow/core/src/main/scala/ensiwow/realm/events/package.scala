package ensiwow.realm

import ensiwow.realm.entities.CharacterRef
import ensiwow.realm.protocol.payloads.ClientMovement
import scodec.bits.BitVector

import scala.collection.mutable

/**
  * Created by sknz on 4/21/17.
  */
package object events {
  sealed trait WorldEvent

  case class PlayerJoined(character: CharacterRef) extends WorldEvent

  case class Tick(number: Long, msTime: Long, previousTick: Tick) extends WorldEvent

  case class DispatchWorldUpdate(events: mutable.MutableList[WorldEvent]) extends WorldEvent

  case class PlayerMoved(payload: ClientMovement, headerBits: BitVector, payloadBits: BitVector) extends WorldEvent
}
