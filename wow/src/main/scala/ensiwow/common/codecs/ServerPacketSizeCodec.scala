package ensiwow.common.codecs

import ensiwow.realm.protocol.OpCodes
import scodec.Attempt.{Failure, Successful}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

/**
  * Encodes a server sent packet size as either a 15 or 23 bytes big endian unsigned integer according to it's value by
  * using a prefix (0 for 2 bytes, 1 for 3 bytes)
  * Also adds header size to encoded value
  */
private[codecs] final class ServerPacketSizeCodec extends Codec[Int] {
  val PrefixLength = 1

  private val SmallPrefix = BitVector.low(PrefixLength)
  private val BigPrefix = BitVector.high(PrefixLength)

  val SmallSize: Int = 15
  val BigSize: Int = 23

  private val PayloadLengthCodecProvider = uint _

  private val SmallCodec = PayloadLengthCodecProvider(SmallSize)
  private val BigCodec = PayloadLengthCodecProvider(BigSize)

  val CodecValueBoundary: Int = math.pow(2, SmallSize).toInt

  override def sizeBound: SizeBound = SmallCodec.sizeBound | BigCodec.sizeBound

  override def encode(payloadLength: Int): Attempt[BitVector] = {
    val totalSize = OpCodes.OpCodeSize / 8 + payloadLength

    val (prefix, codec) = if (totalSize < CodecValueBoundary) {
      (SmallPrefix, SmallCodec)
    } else {
      (BigPrefix, BigCodec)
    }

    for {
      prefixEnc <- constant(prefix).encode(())
      valueEnc <- codec.encode(totalSize)
    } yield {
      prefixEnc ++ valueEnc
    }
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[Int]] = {
    bits.acquire(PrefixLength) match {
      case Left(err) => Attempt.failure(Err(err))
      case Right(prefixBits) =>
        val codec = prefixBits match {
          case SmallPrefix => SmallCodec
          case BigPrefix => BigCodec
        }

        val valueBits = bits.drop(PrefixLength)
        codec.decode(valueBits) match {
          case f: Failure => f
          case Successful(DecodeResult(totalSize, remainder)) =>
            val payloadSize = totalSize - OpCodes.OpCodeSize / 8

            Attempt.Successful(DecodeResult(payloadSize, remainder))
        }
    }
  }

  override def toString = s"ServerPacketSizeCodec"
}
