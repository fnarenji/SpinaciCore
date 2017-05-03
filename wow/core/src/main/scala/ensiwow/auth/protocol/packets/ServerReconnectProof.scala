package ensiwow.auth.protocol.packets

import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.{OpCodes, ServerPacket}
import ensiwow.common.codecs._
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
