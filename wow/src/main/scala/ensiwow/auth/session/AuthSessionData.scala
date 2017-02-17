package ensiwow.auth.session

import ensiwow.auth.crypto.{Srp6Challenge, Srp6Identity}

/**
  * Data
  */
sealed trait AuthSessionData

/**
  * No date by default and in most cases
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
  * @param sessionKey    session key
  */
case class ProofData(challengeData: ChallengeData, sessionKey: BigInt) extends AuthSessionData
