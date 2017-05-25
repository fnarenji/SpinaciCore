package wow.client.auth

import akka.actor.ActorRef
import wow.auth.protocol.packets._
import wow.auth.protocol.{OpCodes, ServerPacket}
import wow.client.{Operation, TestTarget}
import wow.common.VersionInfo

import scala.concurrent.{ExecutionContext, Future}

case class AccountEntry(login: String, password: String) {
  login.toUpperCase
}

/**
  * A client that should mimic a real authentication client
  */
class AuthClient extends TestTarget[AuthClient] {

  var challenge: ServerLogonChallengeSuccess = _

  override def await(opCode: OpCodes.Value): Future[ServerPacket] = {
    implicit val ec = ExecutionContext.Implicits.global
    Future {
      Thread.sleep(2000)
      println(s"Buffer size: ${buffer.length}, opCode: $opCode")
      val packet = opCode match {
        case OpCodes.LogonChallenge => PacketSerializer.deserialize(buffer.head)(ServerLogonChallenge.codec)
        case OpCodes.LogonProof => PacketSerializer.deserialize(buffer.head)(ServerLogonProof.codec)
        case OpCodes.RealmList => PacketSerializer.deserialize(buffer.head)(ServerRealmlist.codec)
      }
      buffer = buffer.drop(1)
      packet
    }
  }
}


/**
  * From the server's challenge, computes a proof that confirms the identity of the client to the
  * server
  * @param account the player's login and password
  * @param challenge the server's challenge
  */
class SendProof(account: AccountEntry, challenge: ServerLogonChallengeSuccess) extends Operation[AuthClient] {
  override def apply(tcpClient: ActorRef): Unit = {
    tcpClient ! writePacket(Srp6Client.computeProof(account, challenge))
  }
}

/**
  * Sends a realmlist request
  */
class SendRealmlistRequest extends Operation[AuthClient] {
  override def apply(tcpClient: ActorRef): Unit = {
    tcpClient ! writePacket(ClientRealmlist())
  }
}

/**
  * Sends a challenge
  * @param ip the client's ip
  * @param login the player's login
  */
class SendChallenge(ip: Vector[Int], login: String) extends Operation[AuthClient] {
  override def apply(tcpClient: ActorRef): Unit =
    tcpClient ! writePacket(challengeRequest)(ClientChallenge.logonChallengeCodec)

  val challengeRequest = ClientChallenge(
    error = 8,
    size = 31,
    VersionInfo.SupportedVersionInfo,
    platform = "x86",
    os = "OSX",
    country = "enUS",
    timezoneBias = 120,
    ip = ip,
    login = login.toUpperCase)
}

