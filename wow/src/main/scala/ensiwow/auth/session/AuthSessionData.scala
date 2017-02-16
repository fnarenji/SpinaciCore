package ensiwow.auth.session

import ensiwow.auth.crypto.{Srp6Challenge, Srp6Identity}

/**
  * Data
  */
sealed trait AuthSessionData

case object NoData extends AuthSessionData

case class ChallengeData(login: String,
                         srp6Identity: Srp6Identity,
                         srp6Challenge: Srp6Challenge) extends AuthSessionData

case class ProofData(challengeData: ChallengeData, sessionKey: BigInt) extends AuthSessionData
