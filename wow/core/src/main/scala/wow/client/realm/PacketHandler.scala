package wow.client.realm

import scodec.bits.BitVector
import scodec.{Codec, DecodeResult, Err}
import wow.auth.protocol.{ClientPacket, OpCodes}
import wow.auth.utils.{MalformedPacketException, PacketPartialReadException}
import wow.realm.protocol.Payload
import wow.utils.Reflection

/**
  * A packet handler is an actor which handles one type of packet
  */
abstract class PacketHandler {
  /**
    * List of OpCodes support by packet handler
    */
  def opCodes: OpCodes.ValueSet

  /**
    * Handle incoming packet
    *
    * @param header      header of packet
    * @param payloadBits payload bits for packet
    */
  def handle(header: ServerHeader, payloadBits: BitVector): Unit

  /**
    * Fails with exception
    *
    * @param e exception
    */
  def fail(e: Throwable): Unit = throw e

  /**
    * Fails with serialization exception
    *
    * @param e scodec error
    */
  def fail(e: Err): Unit = fail(MalformedPacketException(e))
}

/**
  * Entry point for finding/calling packet handlers
  */
object PacketHandler {

  /**
    * Processes packet
    *
    * @param header      header
    * @param payloadBits payload
    */
  def apply(header: ServerHeader, payloadBits: BitVector): Unit = {
    val handlers = new PacketHandlerHelper().handlersByOpCode

    // Unsupported packets will be silently ignored
    if (handlers.contains(header.opCode)) {
      println(s"Got packet $header/${payloadBits.bytes.length}")
      handlers(header.opCode).handle(header, payloadBits)
    } else {
      println(s"Got unhandled packet $header/${payloadBits.bytes.length}")
    }
  }

  /**
    * Helper class for reflection on generic type of packet handler
    */
  private class PacketHandlerHelper() {
    /**
      * List of handlers for tag
      */
    private val handlers = Reflection.objectsOf[PacketHandler]

    /**
      * Map of handlers for tag by opcode
      */
    val handlersByOpCode: Map[OpCodes.Value, PacketHandler] =
      handlers flatMap (x => x.opCodes.toList map (_ -> x)) toMap
  }

}

/**
  * Payload-containing packet handler
  *
  * @tparam A payload type
  */
abstract class PayloadHandler[A <: Payload with ClientPacket]
(override val opCodes: OpCodes.ValueSet)
(implicit codec: Codec[A])
  extends PacketHandler {

  /**
    * Construct from opcodeprovider for packet type
    */
  def this()(implicit opCodeProvider: OpCodeProvider[A], codec: Codec[A]) =
    this(OpCodes.ValueSet(opCodeProvider.opCode))(codec)

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  protected def handle(header: ServerHeader, payload: A): Unit

  override def handle(header: ServerHeader, payloadBits: BitVector): Unit = {
    codec
      .decode(payloadBits)
      .fold(fail, {
        case DecodeResult(payload, BitVector.empty) => handle(header, payload)
        case DecodeResult(_, remainder) => throw PacketPartialReadException(remainder)
      })
  }
}

/**
  * Payload-less packet handler
  */
abstract class IgnorePayloadHandler extends PacketHandler {
  /**
    * Processes packet while ignoring payload
    */
  def handle(header: ServerHeader): Unit

  override def handle(header: ServerHeader, payloadBits: BitVector): Unit = handle(header)
}
