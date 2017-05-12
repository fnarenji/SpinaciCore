package wow.realm.session

import akka.actor.{Actor, ActorLogging}
import scodec.Codec
import scodec.bits.BitVector
import wow.common.network.EventIncoming
import wow.realm.handlers.HandledBy
import wow.realm.protocol.{ClientHeader, PacketHandler, PacketSerialization}
import wow.realm.session.NetworkWorker.HandlePacket

/**
  * Created by sknz on 5/12/17.
  */
trait BufferIncomingPackets extends Actor with ActorLogging {
  this: NetworkWorker =>
  private var unprocessedBits = BitVector.empty
  private var currHeader: Option[ClientHeader] = None

  override def receive: Receive = {
    case EventIncoming(bits) =>
      unprocessedBits = unprocessedBits ++ bits
      processBufferedBits()
  }

  private def processBufferedBits(): Unit = {
    //    log.debug(s"Have ${unprocessedBits.bytes.size} bytes waiting to be processed")
    if (currHeader.isEmpty && unprocessedBits.sizeGreaterThanOrEqual(Codec[ClientHeader].sizeBound.exact.get)) {
      //      log.debug("No header, parsing next one")
      val (header, remaining) = PacketSerialization.incomingHeader(unprocessedBits)(sessionCipher)
      currHeader = Some(header)
      unprocessedBits = remaining
    }

    currHeader match {
      case Some(header) =>
        if (unprocessedBits.bytes.size >= header.payloadSize) {
          // log.debug("Has header and payload, parsing payload")

          val payloadBits = unprocessedBits.take(header.payloadSize * 8L)
          unprocessedBits = unprocessedBits.drop(header.payloadSize * 8L)
          currHeader = None

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

          processBufferedBits()
        } else {
          //          log.debug("Has header, no payload, not enough data")
        }
      case None =>
      //        log.debug("No header, not enough data for next one")
    }
  }

}
