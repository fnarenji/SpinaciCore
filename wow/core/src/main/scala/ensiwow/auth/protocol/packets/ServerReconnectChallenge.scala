package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.common.codecs._
import ensiwow.auth.protocol.{AuthResults, OpCodes, ServerPacket}
import scodec._
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Packet values for successful challenge.
  */
case class ServerReconnectChallengeSuccess(random: BigInt)

object ServerReconnectChallengeSuccess {
  implicit val codec: Codec[ServerReconnectChallengeSuccess] = {
    ("random" | fixedUBigIntL(16)) ::
      constantE(ByteVector.low(16))(bytes)
  }.as[ServerReconnectChallengeSuccess]
}

/**
  * Server logon challenge
  */
case class ServerReconnectChallenge(authResult: AuthResult,
                                success: Option[ServerReconnectChallengeSuccess]) extends ServerPacket {
  require((authResult == AuthResults.Success) == success.nonEmpty)
}

object ServerReconnectChallenge {
  implicit val codec: Codec[ServerReconnectChallenge] = {
    constantE(OpCodes.ReconnectChallenge) ::
      (("authResult" | Codec[AuthResult]) >>:~ { authResult =>
        ("success" | conditional(authResult == AuthResults.Success, Codec[ServerReconnectChallengeSuccess])).hlist
      })
  }.as[ServerReconnectChallenge]
}

