package ensiwow.auth.protocol

import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Created by sknz on 2/8/17.
  */
abstract class AuthPacketTest[T](bytes: ByteVector, reference: T)
                                (implicit val m: reflect.Manifest[T],
                                 implicit val codec: Codec[T]) extends FlatSpec with Matchers {
  private val packetBits = bytes.bits

  m.runtimeClass.getSimpleName must "be fully and correctly hydrated" in {
    val attempt = codec.decode(packetBits)
    assert(attempt.isSuccessful)

    val packet = attempt.require.value

    packet shouldEqual reference
  }
}

class ClientLogonChallengeTest extends AuthPacketTest[ClientLogonChallenge](
  hex"00082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353",
  new ClientLogonChallenge(
    error = 8,
    size = 40,
    versionMajor = 3,
    versionMinor = 3,
    versionPatch = 5,
    build = 12340,
    platform = "x86",
    os = "Win",
    country = "enUS",
    timezoneBias = 60,
    ip = Vector(127, 0, 0, 1),
    login = "SKNZBOGOSS"
  )
)
