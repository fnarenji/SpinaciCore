package wow.auth.protocol.packets

import wow.auth.protocol.{ClientPacket, OpCodes}
import wow.common.codecs._
import scodec.Codec
import scodec.codecs._

/**
  * Created by elh on 08/02/17.
  */
case class ClientReconnectProof(clientKey: BigInt,
                                clientProof: BigInt,
                                unk: BigInt,
                                keyCount: Int) extends ClientPacket

object ClientReconnectProof {
  final val clientKeyLength = 16
  final val clientProofLength = 20
  final val unkLength = 20

  implicit val codec: Codec[ClientReconnectProof] = {
    constantE(OpCodes.ReconnectProof) ::
      ("clientKey" | fixedUBigIntL(clientKeyLength)) ::
      ("clientProof" | fixedUBigIntL(clientProofLength)) ::
      ("unk" | fixedUBigIntL(unkLength)) ::
      ("keyCount" | uint8L)
  }.as[ClientReconnectProof]
}