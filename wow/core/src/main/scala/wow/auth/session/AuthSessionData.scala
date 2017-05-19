package wow.auth.session

import scodec.bits.BitVector
import wow.auth.AuthServer
import wow.auth.crypto.{Srp6Challenge, Srp6Identity}
import wow.auth.protocol.packets.ServerRealmlist
import wow.auth.utils.PacketSerializer

/**
  * Data
  */
sealed trait AuthSessionData

/**
  * No data by default and in most cases
  */
case object NoData extends AuthSessionData

/**
  * Challenge related data
  *
  * @param login         user login
  * @param srp6Identity  identity
  * @param srp6Challenge emitted challenge
  */
case class ChallengeData(
  login: String,
  srp6Identity: Srp6Identity,
  srp6Challenge: Srp6Challenge) extends AuthSessionData

/**
  * Validated proof related data
  *
  * @param challengeData challenge data
  * @param sharedKey     shared key
  */
case class ProofData(challengeData: ChallengeData, sharedKey: BigInt) extends AuthSessionData

/**
  * Data related to the reconnect challenge
  *
  * @param login  login
  * @param random random value used for encryption
  */
case class ReconnectChallengeData(login: String, random: BigInt) extends AuthSessionData

/**
  * Data related to the realms list state
  *
  * @param bits bits of packet
  */
case class RealmsListData(login: String, bits: BitVector) extends AuthSessionData

object RealmsListData {
  def apply(login: String ,charactersPerRealm: Map[Int, Int] = Map.empty): RealmsListData = {
    val realmEntries = for (realm <- AuthServer.realms.values) yield {
      realm.toEntry(charactersPerRealm.getOrElse(realm.id, 0))
    }

    val bits = PacketSerializer.serialize(ServerRealmlist(realmEntries.toStream))

    RealmsListData(login, bits)
  }
}
