package ensiwow.realm.session

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.protocol.{ClientPacket, ServerPacket}
import ensiwow.common.network.Session
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.{Codec, DecodeResult}

import scala.language.postfixOps

/**
  * Handles an auth session
  */
class RealmSession extends Actor with ActorLogging {
  override def receive = PartialFunction.empty

  /**
    * Deserializes a packet of type T from the bitvector
    *
    * @param bits  bitvector from which to read
    * @param codec codec used for deserialization
    * @tparam T type of packet
    * @return deserialized packets
    */
  private def deserialize[T <: ClientPacket](bits: BitVector)(implicit codec: Codec[T]): T = {
    codec.decode(bits) match {
      case Successful(DecodeResult(value, BitVector.empty)) => value
      case Successful(DecodeResult(_, remainder)) => throw PacketPartialReadException(remainder)
      case Failure(err) => throw MalformedPacketException(err)
    }
  }

  /**
    * Serializes a packet of type T to a bitvector
    *
    * @param value packet to be serialized
    * @param codec codec
    * @tparam T packet type
    * @return bit vector containing serialized object
    */
  private def serialize[T <: ServerPacket](value: T)(implicit codec: Codec[T]): BitVector = {
    codec.encode(value) match {
      case Successful(bits) => bits
      case Failure(err) => throw PacketSerializationException(err)
    }
  }
}

object RealmSession extends Session {
  override def props: Props = Props(classOf[RealmSession])

  override def PreferredName = "AuthSession"
}
