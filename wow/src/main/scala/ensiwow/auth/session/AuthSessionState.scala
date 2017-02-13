package ensiwow.auth.session

/**
  * States
  */
sealed trait AuthSessionState

case object StateChallenge extends AuthSessionState

case object StateProof extends AuthSessionState

case object StateFailed extends AuthSessionState

case object StateRealmlist extends AuthSessionState
