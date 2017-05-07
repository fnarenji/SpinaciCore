package wow.auth.protocol.packets

import wow.auth.protocol.AuthResults.AuthResult
import wow.auth.protocol.{OpCodes, ServerPacket}
import wow.common.codecs._
import scodec.codecs._
import scodec._

/**
  * Created by sknz on 2/19/17.
  */
case class ServerReconnectProof(authResult: AuthResult) extends ServerPacket

object ServerReconnectProof {
  implicit val codec: Codec[ServerReconnectProof] = {
    constantE(OpCodes.ReconnectProof) ::
      ("authResult" | Codec[AuthResult]) ::
      constantE(0)(uint16L)
  }.as[ServerReconnectProof]
}
