package wow.client.auth

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import scodec.bits.BitVector
import wow.auth.protocol.ServerPacket
import wow.auth.protocol.packets._
import wow.client.{NewPacket, Operation, TestTarget}
import wow.common.VersionInfo

import scala.collection.immutable.HashMap
import scala.collection.parallel.immutable.ParVector
import scala.concurrent.{Future, Promise}

case class AccountEntry(login: String, password: String) {
  login.toUpperCase
}

object AuthOpCodes extends Enumeration {
  val ServerLogonChallenge, ServerLogonProof, ServerRealmlist = Value
}

/**
  * A client that should mimic a real authentication client
  */
class AuthClient(system: ActorSystem) extends TestTarget[AuthClient] {

  var challenge: ServerLogonChallengeSuccess = _

  import AuthOpCodes._
  val promises = HashMap(
    ServerLogonChallenge -> Promise[ServerPacket],
    ServerLogonProof -> Promise[ServerPacket],
    ServerRealmlist -> Promise[ServerPacket])

  var buffer: ParVector[BitVector] = new ParVector

  val pool = HashMap(
    ServerLogonChallenge -> system.actorOf(Consumer.props(buffer, promises(ServerLogonChallenge))),
    ServerLogonProof -> system.actorOf(Consumer.props(buffer, promises(ServerLogonProof))),
    ServerRealmlist -> system.actorOf(Consumer.props(buffer, promises(ServerRealmlist))))

  override def await(opCode: AuthOpCodes.Value): Future[ServerPacket] = {
    promises(opCode).future
  }
}

class Consumer(var buffer: ParVector[BitVector], p: Promise[ServerPacket]) extends Actor with ActorLogging {
  var slot: BitVector = _

  override def receive: Receive = {
    case NewPacket(challenge) =>
      log.debug("Consumer received NewPacket notification")
      p success challenge
  }
}

object Consumer {
  def props(buffer: ParVector[BitVector], p: Promise[ServerPacket]) = Props(new Consumer(buffer, p))
}


/**
  * From the server's challenge, computes a proof that confirms the identity of the client to the
  * server
  *
  * @param account   the player's login and password
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
  *
  * @param ip    the client's ip
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

