package ensiwow.auth.session

import ensiwow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerReconnectChallenge, ServerReconnectProof}
import scodec.bits.BitVector

/**
  * Events
  */
sealed trait AuthSessionEvent

case class EventPacket(bits: BitVector) extends AuthSessionEvent

case class EventChallengeSuccess(packet: ServerLogonChallenge,
                                 challengeData: ChallengeData) extends AuthSessionEvent

case class EventChallengeFailure(packet: ServerLogonChallenge) extends AuthSessionEvent

case class EventProofFailure(packet: ServerLogonProof) extends AuthSessionEvent

case class EventProofSuccess(packet: ServerLogonProof, proofData: ProofData) extends AuthSessionEvent

case class EventReconnectChallengeSuccess(packet: ServerReconnectChallenge,
                                          reconnectChallengeData: ReconnectChallengeData) extends AuthSessionEvent

case class EventReconnectChallengeFailure(packet: ServerReconnectChallenge) extends AuthSessionEvent

case object EventReconnectProofFailure extends AuthSessionEvent

case class EventReconnectProofSuccess(packet: ServerReconnectProof) extends AuthSessionEvent
