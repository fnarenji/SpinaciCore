package wow.auth.protocol

import scodec._
import scodec.codecs._

/**
  * Enumeration of Auth OpCodes.
  */
object OpCodes extends Enumeration {
  val LogonChallenge = Value(0x0)
  val LogonProof = Value(0x1)
  val ReconnectChallenge = Value(0x2)
  val ReconnectProof = Value(0x3)
  val RealmList = Value(0x10)

  /**
    * Those below are not supported
    */
  val TransferInitiate = Value(0x30)
  val TransferData = Value(0x31)
  val TransferAccept = Value(0x32)
  val TransferResume = Value(0x33)
  val TransferCancel = Value(0x34)

  implicit val codec: Codec[OpCodes.Value] = enumerated(uint8L, OpCodes)
}
