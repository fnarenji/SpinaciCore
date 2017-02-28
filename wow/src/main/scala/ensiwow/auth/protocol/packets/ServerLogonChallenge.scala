package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{AuthResults, OpCodes, ServerPacket}
import scodec._
import scodec.codecs._

/**
  * Packet values for successful challenge.
  */
case class ServerLogonChallengeSuccess(serverKey: BigInt, g: Int, N: BigInt, salt: BigInt, unk3: BigInt)

object ServerLogonChallengeSuccess {
  implicit val codec: Codec[ServerLogonChallengeSuccess] = {
    ("serverKey" | fixedUBigIntL(32)) ::
      constantE(1)(uint8L) ::
      ("g" | uint8L) ::
      constantE(32)(uint8L) ::
      ("N" | fixedUBigIntL(32)) ::
      ("salt" | fixedUBigIntL(32)) ::
      ("unk3" | fixedUBigIntL(16)) ::
      constantE(0)(uint8L)
  }.as[ServerLogonChallengeSuccess]
}

/**
  * Server logon challenge
  */
case class ServerLogonChallenge(authResult: AuthResult,
                                success: Option[ServerLogonChallengeSuccess]) extends ServerPacket {
  require((authResult == AuthResults.Success) == success.nonEmpty)
}

object ServerLogonChallenge {
  implicit val codec: Codec[ServerLogonChallenge] = {
    constantE(OpCodes.LogonChallenge) ::
      constantE(0)(uint8L) :: // no errors
      (("authResult" | Codec[AuthResult]) >>:~ { authResult =>
        ("success" | conditional(authResult == AuthResults.Success, Codec[ServerLogonChallengeSuccess])).hlist
      })
  }.as[ServerLogonChallenge]
}

