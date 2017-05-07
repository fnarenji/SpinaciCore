package wow.realm.protocol.payloads

import wow.common.codecs._
import wow.realm.protocol._
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Addon info
 *
  * @param name name of addon
  * @param enabled is addon enabled
  * @param crc crc of addon
  */
case class AddonInfo(name: String, enabled: Boolean, crc: Long)

object AddonInfo {
  implicit val codec: Codec[AddonInfo] = {
    ("name" | cstring) ::
      ("enabled" | cbool(8)) ::
      ("crc" | uint32L) ::
      ignore(32) // unk1
  }.as[AddonInfo]
}

/**
  * Client auth session packet
  */
case class ClientAuthSession(build: Long,
                             loginServerId: Long,
                             login: String,
                             loginServerType: Long,
                             challenge: Long,
                             regionId: Long,
                             battleGroupId: Long,
                             realmId: Long,
                             dosResponse: BigInt,
                             shaDigest: ByteVector,
                             // Below that, everything is zlib deflated
                             addons: Vector[AddonInfo],
                             currentTime: Long) extends Payload with ClientSide

object ClientAuthSession {
  val AddonInfoMaxSize = 0xFFFFF

  implicit val opCodeProvider: OpCodeProvider[ClientAuthSession] = OpCodes.AuthSession

  implicit val codec: Codec[ClientAuthSession] = {
    ("build" | uint32L) ::
      ("loginServerId" | uint32L) ::
      ("login" | reverse(cstring)) ::
      ("loginServerType" | uint32L) ::
      ("challenge" | uint32L) ::
      ("regionId" | uint32L) ::
      ("battleGroupId" | uint32L) ::
      ("realmId" | uint32L) ::
      ("dosResponse" | fixedUBigIntL(8)) ::
      ("shaDigest" | bytes(20)) ::
      sizePrefixedTransform(
        upperBound(uint32L, AddonInfoMaxSize),
        uint32L.consume {
          addonCount => vectorOfN(provide(addonCount.toInt), Codec[AddonInfo])
        } {
          addons => addons.size.toLong
        } :: ("currentTime" | uint32L),
        zlib(bits)
      )
  }.as[ClientAuthSession]
}
