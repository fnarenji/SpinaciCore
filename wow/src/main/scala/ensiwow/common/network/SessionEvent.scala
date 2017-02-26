package ensiwow.common.network

import scodec.bits.BitVector

/**
  * Session related events
  */
trait SessionEvent

case class EventPacket(bits: BitVector) extends SessionEvent

