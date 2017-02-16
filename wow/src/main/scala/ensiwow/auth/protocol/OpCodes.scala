package ensiwow.auth.protocol

import scodec._
import scodec.codecs._

/**
  * Enumeration of OpCodes.
  */
object OpCodes {
  final case class OpCode(opCode : Int)

  val LogonChallenge = OpCode(0x0)
  val LogonProof = OpCode(0x1)
  val ReconnectChallenge = OpCode(0x2)
  val ReconnectProof = OpCode(0x3)
  val RealmList = OpCode(0x10)

  /**
    * Those below are not supported
    */
  val TransferInitiate = OpCode(0x30)
  val TransferData = OpCode(0x31)
  val TransferAccept = OpCode(0x32)
  val TransferResume = OpCode(0x33)
  val TransferCancel = OpCode(0x34)

  implicit val codec: Codec[OpCode] = { "opCode" | uint8L }.as[OpCode]
}
