package ensiwow.realm.protocol

import ensiwow.common.codecs.{EnumCodecProvider, NumericCodecTag}
import scodec.codecs._

/**
  * Enumerations of response codes
  */

object AuthResponses extends Enumeration with EnumCodecProvider[Int] {
  override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

  val Ok = Value(12)
  val Failed = Value(13)
  val Reject = Value(14)
  val BadServerProof = Value(15)
  val Unavailable = Value(16)
  val SystemError = Value(17)
  val BillingError = Value(18)
  val BillingExpired = Value(19)
  val VersionMismatch = Value(20)
  val UnknownAccount = Value(21)
  val IncorrectPassword = Value(22)
  val SessionExpired = Value(23)
  val ServerShuttingDown = Value(24)
  val AlreadyLoggingIn = Value(25)
  val LoginServerNotFound = Value(26)
  val WaitQueue = Value(27)
  val Banned = Value(28)
  val AlreadyOnline = Value(29)
  val NoTime = Value(30)
  val DbBusy = Value(31)
  val Suspended = Value(32)
  val ParentalControl = Value(33)
  val LockedEnforced = Value(34)
}

object CharacterCreationResults extends Enumeration with EnumCodecProvider[Int] {
  override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

  val Success = Value(47)
  val Error = Value(48)
  val Failed = Value(49)
}

object CharacterDeletionResults extends Enumeration with EnumCodecProvider[Int] {
  override protected val valueCodecTag: NumericCodecTag[Int] = uint8L

  val Success = Value(71)
  val Failure = Value(72)
}

// Other response codec, to be used when necessary in their own enums
//  val ResponseSuccess = Value(0)
//  val ResponseFailure = Value(1)
//  val ResponseCancelled = Value(2)
//  val ResponseDisconnected = Value(3)
//  val ResponseFailedToConnect = Value(4)
//  val ResponseConnected = Value(5)
//  val ResponseVersionMismatch = Value(6)
//
//  val ClientStatusConnecting = Value(7)
//  val ClientStatusNegotiatingSecurity = Value(8)
//  val ClientStatusNegotiationComplete = Value(9)
//  val ClientStatusNegotiationFailed = Value(10)
//  val ClientStatusAuthenticating = Value(11)
//
//
//  val RealmListInProgress = Value(35)
//  val RealmListSuccess = Value(36)
//  val RealmListFailed = Value(37)
//  val RealmListInvalid = Value(38)
//  val RealmListRealmNotFound = Value(39)
//
//  val AccountCreateInProgress = Value(40)
//  val AccountCreateSuccess = Value(41)
//  val AccountCreateFailed = Value(42)
//
//  val CharListRetrieving = Value(43)
//  val CharListRetrieved = Value(44)
//  val CharListFailed = Value(45)
//
//  val CharCreateInProgress = Value(46)
//  val CharCreateSuccess = Value(47)
//  val CharCreateError = Value(48)
//  val CharCreateFailed = Value(49)
//  val CharCreateNameInUse = Value(50)
//  val CharCreateDisabled = Value(51)
//  val CharCreatePvpTeamsViolation = Value(52)
//  val CharCreateServerLimit = Value(53)
//  val CharCreateAccountLimit = Value(54)
//  val CharCreateServerQueue = Value(55)
//  val CharCreateOnlyExisting = Value(56)
//  val CharCreateExpansion = Value(57)
//  val CharCreateExpansionClass = Value(58)
//  val CharCreateLevelRequirement = Value(59)
//  val CharCreateUniqueClassLimit = Value(60)
//  val CharCreateCharacterInGuild = Value(61)
//  val CharCreateRestrictedRaceclass = Value(62)
//  val CharCreateCharacterChooseRace = Value(63)
//  val CharCreateCharacterArenaLeader = Value(64)
//  val CharCreateCharacterDeleteMail = Value(65)
//  val CharCreateCharacterSwapFaction = Value(66)
//  val CharCreateCharacterRaceOnly = Value(67)
//  val CharCreateCharacterGoldLimit = Value(68)
//  val CharCreateForceLogin = Value(69)
//
//  val CharDeleteInProgress = Value(70)
//  val CharDeleteSuccess = Value(71)
//  val CharDeleteFailed = Value(72)
//  val CharDeleteFailedLockedForTransfer = Value(73)
//  val CharDeleteFailedGuildLeader = Value(74)
//  val CharDeleteFailedArenaCaptain = Value(75)
//
//  val CharLoginInProgress = Value(76)
//  val CharLoginSuccess = Value(77)
//  val CharLoginNoWorld = Value(78)
//  val CharLoginDuplicateCharacter = Value(79)
//  val CharLoginNoInstances = Value(80)
//  val CharLoginFailed = Value(81)
//  val CharLoginDisabled = Value(82)
//  val CharLoginNoCharacter = Value(83)
//  val CharLoginLockedForTransfer = Value(84)
//  val CharLoginLockedByBilling = Value(85)
//  val CharLoginLockedByMobileAh = Value(86)
//
//  val CharNameSuccess = Value(87)
//  val CharNameFailure = Value(88)
//  val CharNameNoName = Value(89)
//  val CharNameTooShort = Value(90)
//  val CharNameTooLong = Value(91)
//  val CharNameInvalidCharacter = Value(92)
//  val CharNameMixedLanguages = Value(93)
//  val CharNameProfane = Value(94)
//  val CharNameReserved = Value(95)
//  val CharNameInvalidApostrophe = Value(96)
//  val CharNameMultipleApostrophes = Value(97)
//  val CharNameThreeConsecutive = Value(98)
//  val CharNameInvalidSpace = Value(99)
//  val CharNameConsecutiveSpaces = Value(100)
//  val CharNameRussianConsecutiveSilentCharacters = Value(101)
//  val CharNameRussianSilentCharacterAtBeginningOrEnd = Value(102)
//  val CharNameDeclensionDoesntMatchBaseName = Value(103)
