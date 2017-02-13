package ensiwow.auth.session

/**
  * Data
  */
sealed trait AuthSessionData

trait InitData extends AuthSessionData {
  val g = BigInt(7)
  val N = BigInt("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16)
}

case object InitData extends InitData

case class ChallengeData(s: BigInt, v: BigInt, b: BigInt, B: BigInt) extends InitData

