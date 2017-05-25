package wow.realm.session.network

import akka.actor.{Actor, ActorLogging}
import scodec.Codec
import scodec.bits.BitVector
import wow.realm.protocol._
import wow.realm.session.network.NetworkWorker.HandlePacket

/**
  * Buffers incoming TCP packets and extract game packets from those
  * Works as a finite state machine that needs to composed into NetworkWorker
  */
private[session] trait HandleIncomingPackets extends Actor with ActorLogging with PacketHandlerTag {
  this: NetworkWorker =>

  override def incoming(data: BitVector): Unit = state(data)

  var state: StateFunction = noHeader(BitVector.empty)

  type StateFunction = (BitVector) => Unit

  private final val HeaderSizeBits: Long = Codec[ClientHeader].sizeBound.exact.get

  /**
    * In which not enough bytes have been acquired to read header
    *
    * @param buffer incoming bytes buffer
    * @return next state (self or gotHeader)
    */
  private def noHeader(buffer: BitVector): StateFunction = { bits: BitVector =>
    val newBuffer = buffer ++ bits

    if (newBuffer sizeGreaterThanOrEqual HeaderSizeBits) {
      val (header, payloadBits) = PacketSerialization.incomingHeader(newBuffer)(sessionCipher)

      state = gotHeader(header, payloadBits)
      state(BitVector.empty)
    } else {
      state = noHeader(newBuffer)
    }
  }

  /**
    * In which header has been read, but payload is not fully read
    *
    * @param header read header
    * @param buffer incoming bytes buffer
    * @return next state (self or noHeader)
    */
  private def gotHeader(header: ClientHeader, buffer: BitVector): StateFunction = { bits: BitVector =>
    val newBuffer = buffer ++ bits

    if (newBuffer.bytes.size >= header.payloadSize) {
      handlePayload(header, newBuffer)

      val remainder = newBuffer.drop(header.payloadSize * 8L)

      state = noHeader(remainder)
      state(BitVector.empty)
    } else {
      state = gotHeader(header, newBuffer)
    }
  }

  /**
    * Handles a fully available payload
    *
    * @param header header of payload
    * @param buffer bits in buffer
    */
  private def handlePayload(header: ClientHeader, buffer: BitVector) = {
    buffer.acquire(header.payloadSize * 8L).fold[Unit]({ error: String =>
      throw new IllegalStateException(
        s"Should have had enough bytes in buffer to read whole packet, but read failed (${header.payloadSize} (req) / ${
          buffer.bytes.size} (available)) with error: $error")
    }, {
      payloadBits: BitVector =>
        PacketHandler(header) match {
          case HandledBy.NetworkWorker =>
            PacketHandler(header, payloadBits)(this)
          case HandledBy.Session =>
            session ! HandlePacket(header, payloadBits)
          case HandledBy.Player =>
            player.fold
            { log.info("Got packet for player actor but no actor, ignoring") }
            { player => player ! HandlePacket(header, payloadBits) }
          case HandledBy.Unhandled =>
            log.info(s"Unhandled packet ${header.opCode}")
        }
    })
  }
}

