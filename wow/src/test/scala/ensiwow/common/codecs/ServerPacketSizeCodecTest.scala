package ensiwow.common.codecs

import ensiwow.realm.protocol.OpCodes
import org.scalatest.{FlatSpec, Matchers}
import scodec.bits._

/**
  * Tests server packet size codec
  */
class ServerPacketSizeCodecTest extends FlatSpec with Matchers {
  behavior of "ServerPacketSizeCodec"

  implicit val codec = serverPacketSize

  val boundary = codec.CodecValueBoundary
  val maxValue = math.pow(2, codec.BigSize).toInt - 1

  private def withoutHeaderSize(size: Int) = size - OpCodes.OpCodeSize / 8

  it must "deserialize min value" in CodecTestUtils.decode(hex"0002".bits, withoutHeaderSize(2))
  it must "serialize min value" in CodecTestUtils.encode(hex"0002".bits, withoutHeaderSize(2))

  it must "deserialize max value fitting 15 bit integer" in
    CodecTestUtils.decode(hex"7FFF".bits, withoutHeaderSize(boundary - 1))

  it must "serialize max value fitting 15 bit integer" in
    CodecTestUtils.encode(hex"7FFF".bits, withoutHeaderSize(boundary - 1))

  it must "deserialize lowest value fitting 23 bit integer" in
    CodecTestUtils.decode(hex"808000".bits, withoutHeaderSize(boundary))

  it must "serialize lowest value fitting 23 bit integer" in
    CodecTestUtils.encode(hex"808000".bits, withoutHeaderSize(boundary))

  it must "deserialize max value fitting 23 bit integer" in CodecTestUtils.decode(hex"FFFFFF".bits, withoutHeaderSize(maxValue))
  it must "serialize max value fitting 23 bit integer" in CodecTestUtils.encode(hex"FFFFFF".bits, withoutHeaderSize(maxValue))
}
