package wow.auth.protocol

import scodec.Codec
import scodec.codecs._

/**
  * Enumeration of authentication results.
  */
object AuthResults {
  final case class AuthResult(code : Int)

  val Success = AuthResult(0x00)
  val FailBanned = AuthResult(0x03)
  val FailUnknownAccount = AuthResult(0x04)
  val FailIncorrectPassword = AuthResult(0x05)
  val FailAlreadyOnline = AuthResult(0x06)
  val FailNoTime = AuthResult(0x07)
  val FailDbBusy = AuthResult(0x08)
  val FailVersionInvalid = AuthResult(0x09)
  val FailVersionUpdate = AuthResult(0x0A)
  val FailInvalidServer = AuthResult(0x0B)
  val FailSuspended = AuthResult(0x0C)
  val FailFailNoaccess = AuthResult(0x0D)
  val SuccessSurvey = AuthResult(0x0E)
  val FailParentcontrol = AuthResult(0x0F)
  val FailLockedEnforced = AuthResult(0x10)
  val FailTrialEnded = AuthResult(0x11)
  val FailUseBattlenet = AuthResult(0x12)
  val FailAntiIndulgence = AuthResult(0x13)
  val FailExpired = AuthResult(0x14)
  val FailNoGameAccount = AuthResult(0x15)
  val FailChargeback = AuthResult(0x16)
  val FailInternetGameRoomWithoutBnet = AuthResult(0x17)
  val FailGameAccountLocked = AuthResult(0x18)
  val FailUnlockableLock = AuthResult(0x19)
  val FailConversionRequired = AuthResult(0x20)
  val FailDisconnected = AuthResult(0xFF)

  object AuthResult {
    implicit val codec: Codec[AuthResult] = {
      "code" | uint8L
    }.as[AuthResult]
  }
}

