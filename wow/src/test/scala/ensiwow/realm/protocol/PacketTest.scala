package ensiwow.realm.protocol

import ensiwow.common.codecs.CodecTestUtils
import ensiwow.realm.entities.{EntityType, Guid, GuidType, Position}
import ensiwow.realm.protocol.objectupdates.{UpdateFlags, UpdateType}
import ensiwow.realm.protocol.payloads._
import org.scalatest.{FlatSpec, Matchers}
import scodec.Codec
import scodec.bits._

/**
  * Server packet serialization/deserialization test, without encryption
  */
sealed class PacketTest[TPayload <: Payload, THeader <: PacketHeader](
  headerBytes: ByteVector,
  referenceHeader: THeader,
  payloadBytes: ByteVector,
  referencePayload: TPayload)
  (
    implicit payloadCodec: Codec[TPayload],
    headerCodec: Codec[THeader])
  extends FlatSpec with Matchers {
  behavior of referencePayload.getClass.getSimpleName

  it must "deserialize header as expected" in CodecTestUtils.decode(headerBytes.bits, referenceHeader)
  it must "serialize header as expected" in CodecTestUtils.encode(headerBytes.bits, referenceHeader)

  it must "deserialize payload as expected" in CodecTestUtils.decode(payloadBytes.bits, referencePayload)
  it must "serialize payload as expected" in CodecTestUtils.encode(payloadBytes.bits, referencePayload)

  it must "have matching indicated size and real size" in {
    referenceHeader.payloadSize shouldEqual payloadBytes.length
  }
}

class ServerPacketTest[TPayload <: Payload with ServerSide](
  headerBytes: ByteVector,
  referenceHeader: ServerHeader,
  payloadBytes: ByteVector,
  referencePayload: TPayload)
  (
    implicit payloadCodec: Codec[TPayload],
    opCodeProvider: OpCodeProvider[TPayload])
  extends PacketTest(headerBytes, referenceHeader, payloadBytes, referencePayload)(payloadCodec, Codec[ServerHeader]) {
  it must "serialize as expected" in {
    PacketSerialization.outgoing(referencePayload)(None) shouldEqual (headerBytes ++ payloadBytes).bits
  }
}

class ServerAuthChallengeTest extends PacketTest(
  hex"002AEC01",
  ServerHeader(40, OpCodes.SAuthChallenge),
  hex"01000000550F9060DF17B9B66307EBCAEFD16DC358C98782F58E0E31FFB3EB6EC66EF70D191D42DE",
  ServerAuthChallenge(1620053845,
    BigInt("173504683324832241675852281564110591967"),
    BigInt("295431896831819624089062335876210527989"))
)

class ClientAuthSessionTest extends PacketTest(
  hex"0115ED010000",
  ClientHeader(273, OpCodes.AuthSession),
  hex"3430000000000000540000000000E9DD2585000000000000000001000000020000000000000071BAA088DA240AB1E4F5EB2078E0129908E860D99E020000789C75D2C16AC3300CC671EF2976E99BECB4B450C2EACBE29E8B627F4B446C39384EB7F63DFABE65B70D94F34F48F047AFC69826F2FD4E255CDEFDC8B82241EAB9352FE97B7732FFBC404897D557CEA25A43A54759C63C6F70AD115F8C182C0B279AB52196C032A80BF61421818A4639F5544F79D834879FAAE001FD3AB89CE3A2E0D1EE47D20B1D6DB7962B6E3AC6DB3CEAB2720C0DC9A46A2BCB0CAF1F6C2B5297FD84BA95C7922F59954FE2A082FB2DAADF739C60496880D6DBE509FA13B84201DDC4316E310BCA5F7B7B1C3E9EE193C88D",
  ClientAuthSession(
    build = 12340,
    loginServerId = 0,
    login = "T",
    loginServerType = 0,
    challenge = 2233851369L,
    regionId = 0,
    battleGroupId = 0,
    realmId = 1,
    dosResponse = BigInt(2),
    shaDigest = hex"71BAA088DA240AB1E4F5EB2078E0129908E860D9",
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

class ServerUpdateObjectTest extends PacketTest(
  hex"02C3A900",
  ServerHeader(705, OpCodes.SUpdateObject),
  hex"0100000003010104610000000000000055A6030005A50BC6159FFAC273B4A542E17F293F00000000000020400000E04000009040711C9740000020400000E04000009040E00F4940C3F548402A150080119500C0D8DF83F5010800004A06000406000000000000000000000000000000008202002A000C0F00FC000C0000000000000000000000000000000000000000000000000000000000000000B86DDBB66DDB00000000000000000000000000000000000000000000000000000000000000000000000000000000000020E200100000000000000000000000000000000200FE0000000040000000000080000000003F00000001000000190000000000803F01040003370000006400000037000000E803000064000000E803000001000000010000000800000000080000400600004006000040060000022BC73E0000C03F3100000031000000E32B7E40F2159F40E32BFE3FF2151F40000000000000803F00000000150000001700000015000000140000001400000030000000190000001A0000000E00000066662640333393400000803F060507010000000131000000300000002F0000002C08000087C30000337100000200000000000040060000000000004004000000000000400A000000000000400C0000000000004008000000000000400E0000000000004090010000260000000500050027000000050005005F00000005000500620000002C012C017600000005000500A200000001000500AD00000001000500B000000001000500B700000005000500FD000000050005009E010000010001009F01000001000100F20200000500050009030000010005000A030000010005000200000063FF9F41D36F1D41D36F1D41D36F1D41000000202C0100000000803F0000803F0000803F0000803F0000803F0000803F0000803FFFFFFFFF5000000015000000160000001700000018000000190000001A000000",
  ServerUpdateObject(
    Vector(
      ServerUpdateBlock(
        UpdateType.CreateObject2,
        Guid(1, GuidType.Player),
        EntityType.Player,
        MovementInfo(
          UpdateFlags.Living + UpdateFlags.Self + UpdateFlags.StationaryPosition,
          0,
          0,
          239189,
          Position(None, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
          0
        ),
        MoveSpeeds.DefaultSpeeds,
        hex"2A150080119500C0D8DF83F5010800004A06000406000000000000000000000000000000008202002A000C0F00FC000C0000000000000000000000000000000000000000000000000000000000000000B86DDBB66DDB00000000000000000000000000000000000000000000000000000000000000000000000000000000000020E200100000000000000000000000000000000200FE0000000040000000000080000000003F00000001000000190000000000803F01040003370000006400000037000000E803000064000000E803000001000000010000000800000000080000400600004006000040060000022BC73E0000C03F3100000031000000E32B7E40F2159F40E32BFE3FF2151F40000000000000803F00000000150000001700000015000000140000001400000030000000190000001A0000000E00000066662640333393400000803F060507010000000131000000300000002F0000002C08000087C30000337100000200000000000040060000000000004004000000000000400A000000000000400C0000000000004008000000000000400E0000000000004090010000260000000500050027000000050005005F00000005000500620000002C012C017600000005000500A200000001000500AD00000001000500B000000001000500B700000005000500FD000000050005009E010000010001009F01000001000100F20200000500050009030000010005000A030000010005000200000063FF9F41D36F1D41D36F1D41D36F1D41000000202C0100000000803F0000803F0000803F0000803F0000803F0000803F0000803FFFFFFFFF5000000015000000160000001700000018000000190000001A000000"
      )
    )
  )
)
