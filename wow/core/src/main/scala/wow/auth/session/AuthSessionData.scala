package wow.auth.session

import wow.auth.crypto.{Srp6Challenge, Srp6Identity}

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
case class ChallengeData(login: String,
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
  * @param login login
  * @param random random value used for encryption
  */
case class ReconnectChallengeData(login: String, random: BigInt) extends AuthSessionData
