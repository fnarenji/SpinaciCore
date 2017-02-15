package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.codecs._
import ensiwow.auth.protocol.{AuthResults, OpCodes}
import scodec._
import scodec.codecs._

case class ServerLogonProofFailure()

object ServerLogonProofFailure {
  implicit val codec: Codec[ServerLogonProofFailure] = {
    constantE(3)(uint8L) ::
      constantE(0)(uint8L)
  }.as[ServerLogonProofFailure]
}

case class ServerLogonProofSuccess(M2: BigInt)

object ServerLogonProofSuccess {
  implicit val codec: Codec[ServerLogonProofSuccess] = {
    ("M2" | fixedUBigIntL(20)) ::
      constantE(0x01L)(uint32L) ::
      constantE(0L)(uint32L) ::
      constantE(0)(uint16L)
  }.as[ServerLogonProofSuccess]
}

case class ServerLogonProof(authResult: AuthResult,
                            success: Option[ServerLogonProofSuccess],
                            failure: Option[ServerLogonProofFailure]) {
  require((authResult == AuthResults.Success) == success.nonEmpty)
  require((authResult != AuthResults.Success) == failure.nonEmpty)
}

case object ServerLogonProof {
  implicit val codec: Codec[ServerLogonProof] = {
    constantE(OpCodes.LogonProof) ::
      (("authResult" | Codec[AuthResult]) >>:~ { authResult =>
        ("success" | conditional(authResult == AuthResults.Success, Codec[ServerLogonProofSuccess])) ::
          ("failure" | conditional(authResult != AuthResults.Success, Codec[ServerLogonProofFailure]))
      })
  }.as[ServerLogonProof]
}

