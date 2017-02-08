package ensiwow.auth.protocol

import ensiwow.auth.protocol.codecs._
import scodec._
import scodec.codecs.{fixedSizeBits, _}

/**
  * Created by elh on 08/02/17.
  */
case class ClientReconnectProof(opCode: Int,       // ~cmd
                                BigNumber: String, // ~r1
                                ShaDigest: String, // ~r2
                                r3: String,        // unknown
                                numberOfKeys: Int) {
}

object ClientReconnectProof {
  val reversedFixedCString = reverse(fixedCString)

  final val BigNumberLength = 16
  final val ShaDigestLength = 20

  implicit val codec: Codec[ClientReconnectProof] = {
      ("opCode" | uint8L) ::
      ("BigNumber" | fixedSizeBits(BigNumberLength, reversedFixedCString)) ::
      ("ShaDigest" | fixedSizeBits(ShaDigestLength, reversedFixedCString)) ::
      ("r3" | fixedSizeBits(ShaDigestLength, reversedFixedCString)) ::
      ("numberOfKeys" | fixedSizeBits(ShaDigestLength, reversedFixedCString))
  }.as[ClientReconnectProof]
}