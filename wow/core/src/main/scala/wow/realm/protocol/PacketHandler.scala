package wow.realm.protocol

import akka.actor.{Actor, ActorLogging}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult, Err}
import wow.auth.utils.{MalformedPacketException, PacketPartialReadException}
import wow.realm.RealmContext
import wow.realm.handlers.HandledBy
import wow.utils.Reflection

import scala.collection.mutable

/**
  * Tag used to mark a class as a packet handler
  */
trait PacketHandlerTag extends Actor with ActorLogging with RealmContext

/**
  * A packet handler is an actor which handles one type of packet
  *
  * @tparam A packet handler tagged class
  */
abstract class PacketHandler[A <: PacketHandlerTag] extends Actor with ActorLogging {
  /**
    * List of OpCodes support by packet handler
    */
  def opCodes: OpCodes.ValueSet

  /**
    * Handle incoming packet
    *
    * @param header      header of packet
    * @param payloadBits payload bits for packet
    * @param self        handling context
    */
  def handle(header: ClientHeader, payloadBits: BitVector)(self: A): Unit

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

  import scala.reflect.runtime.universe._

  /**
    * For every opcode, type of context that should be used to handle it
    * (i.e. which actor it's forwarded to)
    */
  private val processorByOpCode: mutable.Map[OpCodes.Value, HandledBy.Value] = {
    val bld = mutable.HashMap.newBuilder[OpCodes.Value, HandledBy.Value]

    for ((typeTag, processedBy) <- HandledBy.TypeTagMap) {
      val handlers = Reflection.objectsOf[PacketHandler[PacketHandlerTag]](typeTag)

      handlers.flatMap(_.opCodes.map(_ -> processedBy)).foreach(bld.+=)
    }

    bld.result()
  }

  /**
    * Get's who processes packet
    *
    * @param header header of packet to be processed
    * @return processed by
    */
  def apply(header: ClientHeader): HandledBy.Value = processorByOpCode.getOrElse(header.opCode, HandledBy.Unhandled)

  /**
    * Processes packet
    *
    * @param header      header
    * @param payloadBits payload
    * @param self        packet handler context
    * @tparam A packet handler context type
    */
  def apply[A <: PacketHandlerTag : TypeTag](header: ClientHeader, payloadBits: BitVector)(self: A): Unit = {
    val handlers = new PacketHandlerHelper[A]().handlersByOpCode

    // Unsupported packets will be silently ignored
    if (handlers.contains(header.opCode)) {
      self.log.debug(s"Got packet $header/${payloadBits.bytes.length}")
      handlers(header.opCode).handle(header, payloadBits)(self)
    } else {
      self.log.info(s"Got unhandled packet $header/${payloadBits.bytes.length}")
    }
  }

  /**
    * Helper class for reflection on generic type of packet handler
    *
    * @param typeTag type tag for packet handler with correct tag
    * @tparam A packet handler tag
    */
  private class PacketHandlerHelper[A <: PacketHandlerTag]()(implicit typeTag: TypeTag[PacketHandler[A]]) {
    /**
      * List of handlers for tag
      */
    private val handlers = Reflection.objectsOf[PacketHandler[A]]

    /**
      * Map of handlers for tag by opcode
      */
    val handlersByOpCode: Map[OpCodes.Value, PacketHandler[A]] =
      handlers flatMap (x => x.opCodes.toList map (_ -> x)) toMap
  }

}

/**
  * Payload-containing packet handler
  *
  * @tparam B payload type
  */
abstract class PayloadHandler[A <: PacketHandlerTag, B <: Payload with ClientSide](override val opCodes: OpCodes
.ValueSet)
  (implicit codec: Codec[B])
  extends PacketHandler[A] {

  /**
    * Construct from opcodeprovider for packet type
    */
  def this()(implicit opCodeProvider: OpCodeProvider[B], codec: Codec[B]) =
    this(OpCodes.ValueSet(opCodeProvider.opCode))(codec)

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  protected def handle(header: ClientHeader, payload: B)(ps: A): Unit

  override def handle(header: ClientHeader, payloadBits: BitVector)(ps: A): Unit = {
    codec
      .decode(payloadBits)
      .fold(fail, {
        case DecodeResult(payload, BitVector.empty) => handle(header, payload)(ps)
        case DecodeResult(_, remainder) => throw PacketPartialReadException(remainder)
      })
  }
}

/**
  * Payload-less packet handler
  */
abstract class IgnorePayloadHandler[A <: PacketHandlerTag] extends PacketHandler[A] {
  /**
    * Processes packet while ignoring payload
    */
  def handle(header: ClientHeader)(ps: A): Unit

  override def handle(header: ClientHeader, payloadBits: BitVector)(ps: A): Unit = handle(header)(ps)
}

