package wow.auth.protocol

import wow.auth.protocol.packets._
import wow.common.VersionInfo
import wow.common.codecs.CodecTestUtils
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Test checking for encoding/decoding idempotency
  */
abstract class AuthPacketTest[A](bytes: ByteVector, reference: A)
                                (implicit val codec: Codec[A]) extends FlatSpec with Matchers {
  private val packetBits = bytes.bits

  behavior of reference.getClass.getSimpleName

  it must "serialize as expected" in CodecTestUtils.encode(packetBits, reference)

  it must "deserialize as expected" in CodecTestUtils.decode(packetBits, reference)
}

abstract class ClientChallengeTest(bytes: ByteVector,
                                   codec: Codec[ClientChallenge]) extends AuthPacketTest[ClientChallenge](bytes,
  ClientChallenge(
    error = 8,
    size = 40,
    versionInfo = VersionInfo(3, 3, 5, 12340),
    platform = "x86",
    os = "Win",
    country = "enUS",
    timezoneBias = 60,
    ip = Vector(127, 0, 0, 1),
    login = "SKNZBOGOSS"
  ))(codec)

class ClientLogonChallengeTest extends ClientChallengeTest(
  hex"00082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353",
  ClientChallenge.logonChallengeCodec)

class ClientReconnectChallengeTest extends ClientChallengeTest(
  hex"02082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353",
  ClientChallenge.reconnectChallengeCodec)

class ServerLogonChallengeSuccessTest extends AuthPacketTest[ServerLogonChallenge](
  ByteVector.fromValidHex("00000053EB4E8B205D34F2536521D6FAC362808CB7106224459654A9928A28B502380E010720B79B3E2A87823C" +
    "AB8F5EBFBF8EB10108535006298B5BADBD5B53E1895E644B897364905AA2F2ED120418BA50F1826244F5694F533F71DE9B0CE359303B8708" +
    "B7ED5D7B90BA7A4BA3D40C147E496AC8F600"),
  ServerLogonChallenge(AuthResults.Success,
    Some(ServerLogonChallengeSuccess(
      serverKey = BigInt("6431342003305856544905171798004231448559842193170150403295401468531275918163"),
      g = 7,
      N = BigInt("62100066509156017342069496140902949863249758336000796928566441170293728648119"),
      salt = BigInt("82788319398741613565528343371422721754703481108946912227584483926043603788915"),
      unk3 = BigInt("328030702092889471223160161530279583213")
    ))
  )
)

class ServerLogonChallengeFailureTest extends AuthPacketTest[ServerLogonChallenge](
  hex"000009",
  ServerLogonChallenge(AuthResults.FailVersionInvalid, None)
)

class ServerLogonChallengeTest extends AuthPacketTest[ServerLogonChallenge](
  ByteVector.fromValidHex("00000053EB4E8B205D34F2536521D6FAC362808CB7106224459654A9928A28B502380E010720B79B3E2A87823C" +
    "AB8F5EBFBF8EB10108535006298B5BADBD5B53E1895E644B897364905AA2F2ED120418BA50F1826244F5694F533F71DE9B0CE359303B8708" +
    "B7ED5D7B90BA7A4BA3D40C147E496AC8F600"),
  ServerLogonChallenge(AuthResults.Success,
    Some(ServerLogonChallengeSuccess(
      serverKey = BigInt("6431342003305856544905171798004231448559842193170150403295401468531275918163"),
      g = 7,
      N = BigInt("62100066509156017342069496140902949863249758336000796928566441170293728648119"),
      salt = BigInt("82788319398741613565528343371422721754703481108946912227584483926043603788915"),
      unk3 = BigInt("328030702092889471223160161530279583213")
    ))
  )
)

class ClientLogonProofTest extends AuthPacketTest[ClientLogonProof](
  ByteVector.fromValidHex("0101BF7357A9D256B8FEA0D3ABC7E478BD61D64D38F7AA4DF0E06F33478894D16FF6625623DB0D80DD472B233B" +
    "8F2D08864CC7CB09B348FF9B4AAC56A778030549A910C0BB2287DE200000"),
  ClientLogonProof(
    clientKey = BigInt("50577022361791655843908772462053474113315524324387888481189784495796664909569"),
    crcHash = BigInt("187650242078200820197416130741145435439049427123"),
    clientProof = BigInt("55925329597235592362644488907692590679176143606")
  )
)

class ServerLogonProofSuccessTest extends AuthPacketTest[ServerLogonProof](
  hex"0100264A77DBF45FA9CD976F6CB2164210EB9864FEDD01000000000000000000",
  ServerLogonProof(AuthResults.Success, Some(ServerLogonProofSuccess(
    serverLogonProof = hex"264A77DBF45FA9CD976F6CB2164210EB9864FEDD"
  )), None)
)

class ServerLogonProofFailureTest extends AuthPacketTest[ServerLogonProof](
  hex"01040300",
  ServerLogonProof(AuthResults.FailUnknownAccount, None, Some(ServerLogonProofFailure()))
)

class ClientRealmlistTest extends AuthPacketTest[ClientRealmlist](
  hex"1000000000",
  ClientRealmlist()
)

class ServerRealmlistTest extends AuthPacketTest[ServerRealmlist](
  hex"1029000000000001000100025472696E697479003132372E302E302E313A3830383500000000000101011000",
  ServerRealmlist(realms = Vector(
        ServerRealmlistEntry(
          realmType = RealmTypes.Pvp,
          lock = false,
          flags = RealmFlags.ValueSet(RealmFlags.Offline),
          name = "Trinity",
          address = "127.0.0.1:8085",
          populationLevel = 0.0f,
          characterCount = 1,
          timeZone = RealmTimeZones.Development,
          id = 1
        )
      ))
)

class ClientReconnectProofTest extends AuthPacketTest[ClientReconnectProof](
  ByteVector.fromValidHex("03A24880D8A707FDEC2A30F9C8C598886D094C05150643E6FC65AC10B08DA21ADC1C645F0E5E512158A04DD5DC" +
    "10CF656F7F27184E195F1D9F00"),
  ClientReconnectProof(
    clientKey = BigInt("145595102509722589026758362009446664354"),
    clientProof = BigInt("82053162634462068115671169198622307468467063817"),
    unk = BigInt("908384538449775762577322799369775676137318469982"),
    keyCount = 0
  )
)

class ServerReconnectChallengeTest extends AuthPacketTest[ServerReconnectChallenge](
  hex"02007907F002A7CFB4675E2DBDAC0A89048D00000000000000000000000000000000",
  ServerReconnectChallenge(
    AuthResults.Success,
    Some(ServerReconnectChallengeSuccess(BigInt("187444696128964725728579388989055698809")))
  )
)

class ServerReconnectProofTest extends AuthPacketTest[ServerReconnectProof](
  hex"03000000",
  ServerReconnectProof(AuthResults.Success)
)

