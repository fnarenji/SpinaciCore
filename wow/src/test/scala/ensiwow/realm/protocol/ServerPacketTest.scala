package ensiwow.realm.protocol

import ensiwow.common.codecs.CodecTestUtils
import ensiwow.realm.protocol.payloads.ServerAuthChallenge
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Server packet serialization/deserialization test, without encryption
  */
sealed class ServerPacketTest[T <: Payload](headerBytes: ByteVector,
                                            referenceHeader: ServerPacketHeader,
                                            payloadBytes: ByteVector,
                                            referencePayload: T)(implicit codec: Codec[T]) extends FlatSpec with Matchers {
  behavior of referencePayload.getClass.getSimpleName

  it must "deserialize header as expected" in CodecTestUtils.decode(headerBytes.bits, referenceHeader)
  it must "serialize header as expected" in CodecTestUtils.encode(headerBytes.bits, referenceHeader)

  it must "deserialize payload as expected" in CodecTestUtils.decode(payloadBytes.bits, referencePayload)
  it must "serialize payload as expected" in CodecTestUtils.encode(payloadBytes.bits, referencePayload)

  it must "serialize as expected" in {
    PacketBuilder.server(referencePayload) shouldEqual (headerBytes ++ payloadBytes).bits
  }
}

class ServerAuthChallengeTest extends ServerPacketTest[ServerAuthChallenge](
  hex"002AEC01",
  ServerPacketHeader(40, OpCodes.SAuthChallenge),
  hex"01000000550F9060DF17B9B66307EBCAEFD16DC358C98782F58E0E31FFB3EB6EC66EF70D191D42DE",
  ServerAuthChallenge(1620053845,
    BigInt("173504683324832241675852281564110591967"),
    BigInt("295431896831819624089062335876210527989"))
)
