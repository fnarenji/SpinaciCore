package ensiwow.auth.handlers

import java.nio.charset.StandardCharsets

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.auth.protocol.AuthResults.AuthResult
import ensiwow.auth.protocol.packets.{ClientLogonChallenge, ServerLogonChallenge, ServerLogonChallengeSuccess}
import ensiwow.auth.protocol.{AuthResults, WotlkVersionInfo}
import ensiwow.auth.session.{ChallengeData, EventChallengeFailure, EventChallengeSuccess}

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
    val userName = packet.login

    // TODO: non hardcoded password
    val password = "t"

    // Auth string is made of uppercase password salted with upper case username
    val authString = s"${userName.toUpperCase}:${password.toUpperCase()}"

    val authBytes = authString.getBytes(StandardCharsets.US_ASCII)
    // Compute shadigest
    val shaDigest = md.digest(authBytes)

    val shaDigestLength = 20
    if (shaDigest.length != shaDigestLength) {
      throw new RuntimeException("Missing padding on shaDigest")
    }
    val reversedShaDigest = shaDigest.reverse

    // Compute random s value for SRP6
    val srp6BitCount = 32 * 8
    val s = BigInt(srp6BitCount, Random)
    val sBytes = s.toByteArray

    // x is sha hash of s and sha digest
    md.update(sBytes)
    val xDigest = md.digest(reversedShaDigest)
    val x = BigInt(xDigest)

    val v = g.modPow(x, N)

    // Compute random b value for SRP6
    val bBitCount = 19 * 8
    val b = BigInt(bBitCount, Random)

    val gMod = g.modPow(b, N)

    val B = ((v * 3) + gMod) % N

    // Compute random unknown value
    val unk3BitCount = 16 * 8
    val unk3 = BigInt(unk3BitCount, Random)

    // Results
    val challengeData = ChallengeData(s, v, b, B)

    val success = ServerLogonChallengeSuccess(B, g.toInt, N, s, unk3)
    val response = ServerLogonChallenge(AuthResults.Success, Some(success))

    (response, challengeData)
  }

  private def validateVersion(packet: ClientLogonChallenge): Option[AuthResult] = {
    val valid = packet.versionMajor == WotlkVersionInfo.Major && packet.versionMinor == WotlkVersionInfo.Minor && packet.versionPatch == WotlkVersionInfo.Patch && packet.build == WotlkVersionInfo.Build

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
