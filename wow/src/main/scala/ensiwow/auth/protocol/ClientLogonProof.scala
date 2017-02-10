package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
case class ClientLogonProof(opCode: Int,
                                A: String,
                                M1: String,
                                crc_hash: String,
                            number_of_keys: Int,
                            securityFlags: Int
                           ) {
  }

object ClientLogonProof {
  val reversedAscii = reverse(ascii)
  val reversedFixedCString = reverse(fixedCString)

  final val ALength = 32
  final val M1Length = 20
  final val CRCLength = 20

  implicit val codec: Codec[ClientLogonProof] = {
      ("opCode" | uint8L) ::
      ("A" | Array[Int](32)) ::
      ("M1" | Array[Int](20)) ::
      ("crc_hash" | Array[Int](20)) ::
      ("number_of_keys" | uint8L) ::
      ("securityFlags" | uint8L)
  }.as[ClientLogonProof]
}
