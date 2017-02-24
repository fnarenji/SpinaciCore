package ensiwow.auth.session

import ensiwow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerRealmlistPacket}
import scodec.bits.BitVector

/**
  * Events
  */
sealed trait AuthSessionEvent

case class EventPacket(bits: BitVector) extends AuthSessionEvent

case class EventChallengeSuccess(packet: ServerLogonChallenge,
                                 challengeData: ChallengeData) extends AuthSessionEvent

case class EventChallengeFailure(packet: ServerLogonChallenge) extends AuthSessionEvent

case class EventLogonFailure(packet: ServerLogonProof) extends AuthSessionEvent

case class EventLogonSuccess(packet: ServerLogonProof, proofData: ProofData) extends AuthSessionEvent

class EventRealmlist extends AuthSessionEvent
case class EventRealmlistSuccess(packet: ServerRealmlistPacket) extends EventRealmlist
case class EventRealmlistFailure() extends EventRealmlist
