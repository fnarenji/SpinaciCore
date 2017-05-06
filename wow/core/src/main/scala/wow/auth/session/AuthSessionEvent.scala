package wow.auth.session

import wow.auth.protocol.packets.{ServerLogonChallenge, ServerLogonProof, ServerReconnectChallenge, ServerReconnectProof}
import wow.common.network.SessionEvent
import scodec.bits.BitVector

/**
  * Events
  */
sealed trait AuthSessionEvent extends SessionEvent
