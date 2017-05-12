package wow.realm.session

import akka.actor.{Actor, ActorLogging}
import scodec.Codec
import scodec.bits.BitVector
import wow.common.network.EventIncoming
import wow.realm.handlers.HandledBy
import wow.realm.protocol.{ClientHeader, PacketHandler, PacketHandlerTag, PacketSerialization}
import wow.realm.session.NetworkWorker.HandlePacket

/**
  * Buffers incoming TCP packets and extract game packets from those
  * Works as a finite state machine that needs to composed into NetworkWorker
  */
private[session] trait HandleIncomingPackets extends Actor with ActorLogging with PacketHandlerTag {
  this: NetworkWorker =>

  override def receive: Receive = {
    case EventIncoming(bits) => state(bits)
  }

  var state: StateFunction = noHeader(BitVector.empty)

  type StateFunction = (BitVector) => Unit

  private final val HeaderSize: Long = Codec[ClientHeader].sizeBound.exact.get

  private def noHeader(buffer: BitVector): StateFunction = { bits: BitVector =>
    val newBuffer = buffer ++ bits

    if (newBuffer sizeGreaterThanOrEqual HeaderSize) {
      val (header, payloadBits) = PacketSerialization.incomingHeader(newBuffer)(sessionCipher)

      state = gotHeader(header, payloadBits)
      state(BitVector.empty)
    }

    state = noHeader(newBuffer)
  }

  private def gotHeader(header: ClientHeader, buffer: BitVector): StateFunction = { bits: BitVector =>
    val newBuffer = buffer ++ bits

    if (newBuffer.bytes.size >= header.payloadSize) {
      handlePayload(header, newBuffer)

      val remainder = newBuffer.drop(header.payloadSize * 8L)

      state = noHeader(remainder)
      state(BitVector.empty)
    }

    state = gotHeader(header, newBuffer)
  }

  private def handlePayload(header: ClientHeader, newBuffer: BitVector) = {
    val payloadBits = newBuffer.take(header.payloadSize * 8L)

    PacketHandler(header) match {
      case HandledBy.NetworkWorker =>
        PacketHandler(header, payloadBits)(this)
      case HandledBy.Session =>
        session ! HandlePacket(header, payloadBits)
      case HandledBy.Player =>
        player ! HandlePacket(header, payloadBits)
      case HandledBy.Unhandled =>
        log.info(s"Unhandled packet ${header.opCode}")
    }
  }
}

