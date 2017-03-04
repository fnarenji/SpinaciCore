package ensiwow.realm.protocol.payloads

import scodec.Codec
import scodec.codecs._
import ensiwow.common.codecs._
import ensiwow.realm.protocol.{OpCodes, Payload, ServerHeader}

/**
  * Server authentication challenge
  */
case class ServerAuthChallenge(authSeed: Long, firstSeed: BigInt, secondSeed: BigInt) extends Payload[ServerHeader] {
  override def opCode: OpCodes.Value = OpCodes.SAuthChallenge
}

object ServerAuthChallenge {
  val SeedSize = 16

  implicit val codec: Codec[ServerAuthChallenge] = {
    constantE(1L)(uint32L) ::
      ("authSeed" | uint32L) ::
      ("firstSeed" | fixedUBigIntL(SeedSize)) ::
      ("secondSeed" | fixedUBigIntL(SeedSize))
  }.as[ServerAuthChallenge]
}
