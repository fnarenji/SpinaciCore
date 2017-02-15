package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
case class ClientLogonProof(A: BigInt,
                            M1: BigInt,
                            crcHash: BigInt,
                            keyCount: Int,
                            securityFlags: Int)

object ClientLogonProof {
  final val ALength = 32
  final val M1Length = 20
  final val CRCLength = 20

  implicit val codec: Codec[ClientLogonProof] = {
      constantE(OpCodes.LogonProof) ::
      ("A" | fixedUBigIntL(ALength)) ::
      ("M1" | fixedUBigIntL(M1Length)) ::
      ("crcHash" | fixedUBigIntL(CRCLength)) ::
      ("keyCount" | uint8L) ::
      ("securityFlags" | uint8L)
  }.as[ClientLogonProof]
}
