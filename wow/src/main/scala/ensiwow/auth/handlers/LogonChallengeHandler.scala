package ensiwow.auth.handlers

import java.nio.charset.StandardCharsets

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.codecs.fixedUBigIntL
import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import ensiwow.auth.protocol.{AuthResults, WotlkVersionInfo}
import ensiwow.auth.session.{ChallengeData, EventChallengeFailure, EventChallengeSuccess}
import scodec.bits.BitVector

import scala.util.Random

case class LogonChallenge(packet: ClientLogonChallenge, g: BigInt, N: BigInt)

/**
  * Handles logon challenges
  */
class LogonChallengeHandler extends Actor with ActorLogging {
  // Obtains a SHA-1 digest instance
  // Note that each call to getInstance returns a different instance
  val md = java.security.MessageDigest.getInstance("SHA-1")

  override def receive: PartialFunction[Any, Unit] = {
    case LogonChallenge(packet, g, _N) =>
      val error = validateVersion(packet)
      val event = error match {
        case Some(authResult) =>
          val challenge = ServerLogonChallenge(authResult, None)

          log.debug(s"Challenge failure response: $challenge")

          EventChallengeFailure(challenge)
        case None =>
          val (response, challengeData) = computeResponse(packet, g, _N)
          log.debug(s"Challenge success response: $response")
          log.debug(s"Challenge values: $challengeData")

          EventChallengeSuccess(response, challengeData)
      }

      sender ! event
  }

  private def computeResponse(packet: ClientLogonChallenge, g: BigInt, N: BigInt) = {
    def toHexB(vl: BigInt) = fixedUBigIntL(32).encode(vl).require.reverseByteOrder.toHex
    def toHexA(vl: Array[Byte]) = BitVector(vl).toHex

    def bigIntEncode(size : Int, value: BigInt) = fixedUBigIntL(size).encode(value).require.toByteArray
    def bigIntDecode(value: Array[Byte]) = fixedUBigIntL(value.length).decode(BitVector(value)).require.value

    val ShaDigestLength = 20
    val Srp6ByteCount = 32
    val Srp6BitCount = Srp6ByteCount * 8
    val SmallBBitCount = 19 * 8
    val Unk3BitCount = 16 * 8
    val DebugMode = true

    // These are fixed values for s, b and unk3.
    // This is only used for testing purposes, otherwise these values should absolutely be randomly generated
    val FixedS = BigInt("78823503796391676434485569088161368409945032487538050771151147647624579312285")
    val Fixedb = BigInt("5698844817725982222235344496777980655886111343")
    val FixedUnk3 = BigInt("194942597757323744367948666173918899059")

    val (s, smallB, unk3) = if (DebugMode) {
      // TODO: with some proper mocking this could be safely moved to unit tests
      (FixedS, Fixedb, FixedUnk3)
    } else {
      (BigInt(Srp6BitCount, Random), BigInt(SmallBBitCount, Random), BigInt(Unk3BitCount, Random))
    }

    assert(s > 0)
    assert(smallB > 0)
    assert(unk3 > 0)

    val userName = packet.login

    // TODO: non hardcoded password
    val password = "t"

    // Auth string is made of uppercase password salted with upper case username
    val authString = s"${userName.toUpperCase}:${password.toUpperCase()}"
    val authBytes = authString.getBytes(StandardCharsets.US_ASCII)
    // Compute authBytes SHA digest
    val authDigest = md.digest(authBytes)
    assert(authDigest.length == ShaDigestLength)

    val sBytes = bigIntEncode(Srp6ByteCount, s)

    // x is sha hash of s and auth digest
    md.update(sBytes)
    md.update(authDigest)
    val xDigest = md.digest()
    assert(xDigest.length == ShaDigestLength)
    val x = bigIntDecode(xDigest)

    val v = g.modPow(x, N)
    val gMod = g.modPow(smallB, N)
    val bigB = ((v * 3) + gMod) % N

    // TODO: with some proper mocking this could be safely moved to unit tests
    if (DebugMode) {
      assert(v == BigInt("22075422366936545515674385768650057420632007409200282980604073279761866516199"))
      assert(gMod == BigInt("29939248540161975887379129838367961798422604125188925685493670515789973246671"))
      assert(bigB == BigInt("34065449131815595092332791003415184197068868016788977698739449184781844147149"))
      assert(x == BigInt("550943625525122196566962046154797572093457682231"))
    }

    // Results
    val challengeData = ChallengeData(s, v, smallB, bigB)

    val success = ServerLogonChallengeSuccess(bigB, g.toInt, N, s, unk3)
    val response = ServerLogonChallenge(AuthResults.Success, Some(success))

    (response, challengeData)
  }

  private def validateVersion(packet: ClientLogonChallenge): Option[AuthResult] = {
    val valid = packet.versionMajor == WotlkVersionInfo.Major &&
      packet.versionMinor == WotlkVersionInfo.Minor &&
      packet.versionPatch == WotlkVersionInfo.Patch &&
      packet.build == WotlkVersionInfo.Build

    if (!valid) {
      Some(AuthResults.FailVersionInvalid)
    } else {
      None
    }
  }
}

object LogonChallengeHandler {
  val PreferredName = "LogonChallengeHandler"

  def props: Props = Props(classOf[LogonChallengeHandler])
}
