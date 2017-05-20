package wow.client.realm

import scodec.Codec
import scodec.bits.BitVector

class HandleIncomingPackets {

  var state: StateFunction = noHeader(BitVector.empty)

  type StateFunction = (BitVector) => Unit

  private final val HeaderSizeBits: Long = Codec[ServerHeader].sizeBound.exact.get

  /**
    * In which not enough bytes have been acquired to read header
    *
    * @param buffer incoming bytes buffer
    * @return next state (self or gotHeader)
    */
  private def noHeader(buffer: BitVector): StateFunction = { bits: BitVector =>
    val newBuffer = buffer ++ bits

    if (newBuffer sizeGreaterThanOrEqual HeaderSizeBits) {
      val (header, payloadBits) = PacketSerialization.incomingHeader(newBuffer)(None)

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
  private def gotHeader(header: ServerHeader, buffer: BitVector): StateFunction = { bits: BitVector =>
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
  private def handlePayload(header: ServerHeader, buffer: BitVector) = {
    buffer.acquire(header.payloadSize * 8L).fold[Unit]({ error: String =>
      throw new IllegalStateException(
        s"Should have had enough bytes in buffer to read whole packet, but read failed (${header.payloadSize} (req) / ${
          buffer.bytes.size} (available)) with error: $error")
    }, {
      payloadBits: BitVector =>
        PacketHandler(header, payloadBits)
    })
  }
}
