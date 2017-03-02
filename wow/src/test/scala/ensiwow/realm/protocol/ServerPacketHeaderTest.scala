package ensiwow.realm.protocol

import ensiwow.common.codecs.CodecTestUtils
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Test for Server Packet Header parsing
  */
class ServerPacketHeaderTest extends FlatSpec with Matchers {
  implicit val codec = Codec[ServerPacketHeader]

  behavior of "ServerPacketHeader"

  private val header = ServerPacketHeader(40, OpCodes.SAuthChallenge)
  private val bits = hex"002AEC01".bits

  it must "serialize as expected" in CodecTestUtils.encode(bits, header)
  it must "deserialize as expected" in CodecTestUtils.decode(bits, header)
}
