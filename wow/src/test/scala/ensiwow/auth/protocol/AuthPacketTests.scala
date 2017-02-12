package ensiwow.auth.protocol

import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import org.scalatest.{FlatSpec, Matchers}
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scodec.{Codec, DecodeResult}

/**
  * Created by sknz on 2/8/17.
  */
abstract class AuthPacketTest[T](bytes: ByteVector, reference: T)
                                (implicit val m: reflect.Manifest[T],
                                 implicit val codec: Codec[T]) extends FlatSpec with Matchers {
  private val packetBits = bytes.bits

  m.runtimeClass.getSimpleName must "be fully and correctly hydrated" in {
    val attempt = codec.decode(packetBits)
    attempt match {
      case Failure(err) => fail(err.toString())
      case Successful(DecodeResult(packet, BitVector.empty)) =>
        packet shouldEqual reference

        val encode = codec.encode(packet)
        encode match {
          case Successful(bits) => bits shouldEqual packetBits
          case Failure(err) => fail(err.toString())
        }
      case Successful(DecodeResult(_, remainder)) => fail(s"non empty remainder: $remainder")
    }
  }
}

class ClientLogonChallengeTest extends AuthPacketTest[ClientLogonChallenge](
  hex"00082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353",
  ClientLogonChallenge(
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

class ServerLogonChallengeTest extends AuthPacketTest[ServerLogonChallenge](
  hex"00000053EB4E8B205D34F2536521D6FAC362808CB7106224459654A9928A28B502380E010720B79B3E2A87823CAB8F5EBFBF8EB10108535006298B5BADBD5B53E1895E644B897364905AA2F2ED120418BA50F1826244F5694F533F71DE9B0CE359303B8708B7ED5D7B90BA7A4BA3D40C147E496AC8F600",
  ServerLogonChallenge(AuthResults.Success,
    Some(ServerLogonChallengeSuccess(
      B = BigInt("6431342003305856544905171798004231448559842193170150403295401468531275918163"),
      g = 7,
      N = BigInt("62100066509156017342069496140902949863249758336000796928566441170293728648119"),
      s = BigInt("82788319398741613565528343371422721754703481108946912227584483926043603788915"),
      unk3 = BigInt("328030702092889471223160161530279583213")
    ))
  )
)
