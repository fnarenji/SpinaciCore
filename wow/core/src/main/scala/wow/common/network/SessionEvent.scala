package wow.common.network

import scodec.bits.BitVector

/**
  * Session related events
  */
trait SessionEvent

/**
  * Incoming packet read from the network
  * @param bits bits read
  */
case class EventIncoming(bits: BitVector) extends SessionEvent

