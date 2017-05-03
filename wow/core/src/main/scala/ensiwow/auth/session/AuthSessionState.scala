package ensiwow.auth.session

/**
  * States
  */
sealed trait AuthSessionState

case object StateNoData extends AuthSessionState

case object StateChallenge extends AuthSessionState

case object StateProof extends AuthSessionState

case object StateFailed extends AuthSessionState

case object StateRealmlist extends AuthSessionState

case object StateReconnectChallenge extends AuthSessionState

case object StateReconnectProof extends AuthSessionState
