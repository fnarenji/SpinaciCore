package ensiwow.realm.protocol

import ensiwow.common.codecs.CodecTestUtils
import ensiwow.realm.protocol.payloads.{AddonInfo, ClientAuthSession, ServerAuthChallenge}
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Server packet serialization/deserialization test, without encryption
  */
sealed class PacketTest[TPayload <: Payload[THeader], THeader <: PacketHeader](headerBytes: ByteVector,
                                                                               referenceHeader: THeader,
                                                                               payloadBytes: ByteVector,
                                                                               referencePayload: TPayload)
                                                                              (implicit payloadCodec: Codec[TPayload],
                                                                               headerCodec: Codec[THeader])
  extends FlatSpec with Matchers {
  behavior of referencePayload.getClass.getSimpleName

  it must "deserialize header as expected" in CodecTestUtils.decode(headerBytes.bits, referenceHeader)
  it must "serialize header as expected" in CodecTestUtils.encode(headerBytes.bits, referenceHeader)

  it must "deserialize payload as expected" in CodecTestUtils.decode(payloadBytes.bits, referencePayload)
  it must "serialize payload as expected" in CodecTestUtils.encode(payloadBytes.bits, referencePayload)
}

class ServerPacketTest[TPayload <: Payload[ServerHeader]](headerBytes: ByteVector,
                                                          referenceHeader: ServerHeader,
                                                          payloadBytes: ByteVector,
                                                          referencePayload: TPayload)
                                                         (implicit payloadCodec: Codec[TPayload],
                                                          opCodeProvider: OpCodeProvider[TPayload])
  extends PacketTest(headerBytes, referenceHeader, payloadBytes, referencePayload)(payloadCodec, Codec[ServerHeader]) {
  it must "serialize as expected" in {
    PacketSerialization.outgoing(referencePayload)(None) shouldEqual (headerBytes ++ payloadBytes).bits
  }
}

class AuthChallengeTest extends PacketTest(
  hex"002AEC01",
  ServerHeader(40, OpCodes.SAuthChallenge),
  hex"01000000550F9060DF17B9B66307EBCAEFD16DC358C98782F58E0E31FFB3EB6EC66EF70D191D42DE",
  ServerAuthChallenge(1620053845,
    BigInt("173504683324832241675852281564110591967"),
    BigInt("295431896831819624089062335876210527989"))
)

class ClientAuthSessionTest extends PacketTest(
  hex"0111ED010000",
  ClientHeader(269, OpCodes.AuthSession),
  hex"3430000000000000540000000000B03BE72D00000000000000000100000000000000000000009C8EBF04888EA06E1E93418C9F15CF96E01404D39E020000789C75D2C16AC3300CC671EF2976E99BECB4B450C2EACBE29E8B627F4B446C39384EB7F63DFABE65B70D94F34F48F047AFC69826F2FD4E255CDEFDC8B82241EAB9352FE97B7732FFBC404897D557CEA25A43A54759C63C6F70AD115F8C182C0B279AB52196C032A80BF61421818A4639F5544F79D834879FAAE001FD3AB89CE3A2E0D1EE47D20B1D6DB7962B6E3AC6DB3CEAB2720C0DC9A46A2BCB0CAF1F6C2B5297FD84BA95C7922F59954FE2A082FB2DAADF739C60496880D6DBE509FA13B84201DDC4316E310BCA5F7B7B1C3E9EE193C88D",
  ClientAuthSession(
    build = 12340,
    loginServerId = 0,
    login = "T",
    loginServerType = 0,
    challenge = 770128816,
    regionId = 0,
    battleGroupId = 0,
    realmId = 1,
    dosResponse = BigInt(0),
    shaDigest = hex"9C8EBF04888EA06E1E93418C9F15CF96E01404D3",
    addons = Vector(
      AddonInfo("Blizzard_AchievementUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_ArenaUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_AuctionUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_BarbershopUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_BattlefieldMinimap", enabled = true, 1276933997),
      AddonInfo("Blizzard_BindingUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_Calendar", enabled = true, 1276933997),
      AddonInfo("Blizzard_CombatLog", enabled = true, 1276933997),
      AddonInfo("Blizzard_CombatText", enabled = true, 1276933997),
      AddonInfo("Blizzard_DebugTools", enabled = true, 1276933997),
      AddonInfo("Blizzard_GMChatUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_GMSurveyUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_GlyphUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_GuildBankUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_InspectUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_ItemSocketingUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_MacroUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_RaidUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_TalentUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_TimeManager", enabled = true, 1276933997),
      AddonInfo("Blizzard_TokenUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_TradeSkillUI", enabled = true, 1276933997),
      AddonInfo("Blizzard_TrainerUI", enabled = true, 1276933997)
    ),
    1262785851
  )
)