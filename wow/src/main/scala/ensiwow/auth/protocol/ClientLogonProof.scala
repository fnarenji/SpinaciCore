package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
case class ClientLogonProof(opCode: Int,
                            A: BigInt,
                            M1: BigInt,
                            crc_hash: BigInt,
                            number_of_keys: Int,
                            securityFlags: Int)

object ClientLogonProof {
  final val ALength = 32
  final val M1Length = 20
  final val CRCLength = 20

  implicit val codec: Codec[ClientLogonProof] = {
    ("opCode" | uint8L) ::
      ("A" | fixedUBigIntL(ALength)) ::
      ("M1" | fixedUBigIntL(M1Length)) ::
      ("crc_hash" | fixedUBigIntL(CRCLength)) ::
      ("number_of_keys" | uint8L) ::
      ("securityFlags" | uint8L)
  }.as[ClientLogonProof]
}
