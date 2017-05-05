package ensiwow.realm.protocol

import ensiwow.common.codecs.CodecTestUtils
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Test for Packet Header parsing
  */
class ServerHeaderTest extends FlatSpec with Matchers {
  behavior of "ServerPacketHeader"

  private val header = ServerHeader(40, OpCodes.SAuthChallenge)
  private val bits = hex"002AEC01".bits

  it must "serialize as expected" in CodecTestUtils.encode(bits, header)
  it must "deserialize as expected" in CodecTestUtils.decode(bits, header)
}

class ClientHeaderTest extends FlatSpec with Matchers {
  behavior of "ClientPacketHeader"

  private val header = ClientHeader(269, OpCodes.AuthSession)
  private val bits = hex"0111ED010000".bits

  it must "serialize as expected" in CodecTestUtils.encode(bits, header)
  it must "deserialize as expected" in CodecTestUtils.decode(bits, header)
}
