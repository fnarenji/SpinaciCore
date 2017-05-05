package ensiwow.common.codecs

import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Tests server packet size codec
  */
class ServerPacketSizeCodecTest extends FlatSpec with Matchers {
  behavior of "serverPacketSizeCodec"

  implicit val codec: Codec[Int] = serverPacketSizeCodec

  val boundary: Int = math.pow(2, 15).toInt
  val maxValue: Int = math.pow(2, 23).toInt - 1

  it must "deserialize min value" in CodecTestUtils.decode(hex"0002".bits, 2)
  it must "serialize min value" in CodecTestUtils.encode(hex"0002".bits, 2)

  it must "deserialize max value fitting 15 bit integer" in
    CodecTestUtils.decode(hex"7FFF".bits, boundary - 1)

  it must "serialize max value fitting 15 bit integer" in
    CodecTestUtils.encode(hex"7FFF".bits, boundary - 1)

  it must "deserialize lowest value fitting 23 bit integer" in
    CodecTestUtils.decode(hex"808000".bits, boundary)

  it must "serialize lowest value fitting 23 bit integer" in
    CodecTestUtils.encode(hex"808000".bits, boundary)

  it must "deserialize max value fitting 23 bit integer" in CodecTestUtils.decode(hex"FFFFFF".bits, maxValue)
  it must "serialize max value fitting 23 bit integer" in CodecTestUtils.encode(hex"FFFFFF".bits, maxValue)
}
