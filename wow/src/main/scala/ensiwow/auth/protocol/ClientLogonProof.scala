package ensiwow.auth.protocol

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
                            securityFlags: Int
                           ) {
  }

object ClientLogonProof {

  final val ALength = 32
  final val M1Length = 20
  final val CRCLength = 20

  implicit val codec: Codec[ClientLogonProof] = {
      ("opCode" | uint8L) ::
      ("A" | fixedUBigIntL(32)) ::
      ("M1" | fixedUBigIntL(20)) ::
      ("crc_hash" | fixedUBigIntL(20)) ::
      ("number_of_keys" | uint8L) ::
      ("securityFlags" | uint8L)
  }.as[ClientLogonProof]
}
