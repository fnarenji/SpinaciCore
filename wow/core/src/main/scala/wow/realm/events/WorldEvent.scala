package wow.realm.events

import scodec.bits.BitVector
import wow.realm.entities.CharacterRef
import wow.realm.protocol.payloads.ClientMovement

import scala.collection.mutable

/**
  * World events
  */
sealed trait WorldEvent

case class PlayerJoined(character: CharacterRef) extends WorldEvent

case class Tick(number: Long, msTime: Long, previousTick: Tick) extends WorldEvent

case class DispatchWorldUpdate(events: mutable.MutableList[WorldEvent]) extends WorldEvent

case class PlayerMoved(payload: ClientMovement, headerBits: BitVector, payloadBits: BitVector) extends WorldEvent
