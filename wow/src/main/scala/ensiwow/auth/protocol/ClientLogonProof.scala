package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs._

/**
  * Created by sknz on 2/7/17.
  */
case class ClientLogonProof(opCode: Int,
                                A: Vector[Int],
                                M1: Vector[Int],
                                crc_hash: Vector[Int],
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
      ("A" | vectorOfN(provide(32), uint8L)) ::
      ("M1" | vectorOfN(provide(20), uint8L)) ::
      ("crc_hash" | vectorOfN(provide(20), uint8L)) ::
      ("number_of_keys" | uint8L) ::
      ("securityFlags" | uint8L)
  }.as[ClientLogonProof]
}
