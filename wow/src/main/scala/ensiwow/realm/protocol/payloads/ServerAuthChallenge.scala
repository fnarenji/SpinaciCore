package ensiwow.realm.protocol.payloads

import ensiwow.common.codecs._
import ensiwow.realm.protocol.{OpCodeProvider, OpCodes, Payload, ServerHeader}
import scodec.Codec
import scodec.codecs._

/**
  * Server authentication challenge
  */
case class ServerAuthChallenge(authSeed: Long, firstSeed: BigInt, secondSeed: BigInt) extends Payload[ServerHeader]

object ServerAuthChallenge {
  val SeedSize = 16

  implicit val opCodeProvider: OpCodeProvider[ServerAuthChallenge]  = OpCodes.SAuthChallenge

  implicit val codec: Codec[ServerAuthChallenge] = {
    constantE(1L)(uint32L) ::
      ("authSeed" | uint32L) ::
      ("firstSeed" | fixedUBigIntL(SeedSize)) ::
      ("secondSeed" | fixedUBigIntL(SeedSize))
  }.as[ServerAuthChallenge]
}
