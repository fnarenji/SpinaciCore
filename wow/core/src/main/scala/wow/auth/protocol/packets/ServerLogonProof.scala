package wow.auth.protocol.packets

import wow.auth.protocol.AuthResults.AuthResult
import wow.common.codecs._
import wow.auth.protocol.{AuthResults, OpCodes, ServerPacket}
import scodec._
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Empty case class used for serialization purposes
  */
case class ServerLogonProofFailure()

object ServerLogonProofFailure {
  implicit val codec: Codec[ServerLogonProofFailure] = {
    constantE(3)(uint8L) ::
      constantE(0)(uint8L)
  }.as[ServerLogonProofFailure]
}

/**
  * Packet containing information about successful logon proof.
  */
case class ServerLogonProofSuccess(serverLogonProof: ByteVector)

object ServerLogonProofSuccess {
  private val ShaDigestLength = java.security.MessageDigest.getInstance("SHA-1").getDigestLength

  implicit val codec: Codec[ServerLogonProofSuccess] = {
    ("serverLogonProof" | bytes(ShaDigestLength)) ::
      constantE(0x01L)(uint32L) ::
      constantE(0L)(uint32L) ::
      constantE(0)(uint16L)
  }.as[ServerLogonProofSuccess]
}

/**
  * Server logon proof packet, after client logon proof.
  */
case class ServerLogonProof(authResult: AuthResult,
                            success: Option[ServerLogonProofSuccess],
                            failure: Option[ServerLogonProofFailure]) extends ServerPacket {
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

