package wow.client.auth

import akka.actor.{ActorRef, ActorSystem}
import wow.auth.protocol.ServerPacket
import wow.auth.protocol.packets._
import wow.client.{Consumer, Operation, TestTarget}
import wow.common.VersionInfo

import scala.collection.immutable.HashMap
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

case class AccountEntry(login: String, password: String) {
  login.toUpperCase
}

object AuthOpCodes extends Enumeration {
  val ServerLogonChallenge, ServerLogonProof, ServerRealmlist = Value
}

/**
  * A client that should mimic a real authentication client
  */
class AuthClient(system: ActorSystem, promises: HashMap[AuthOpCodes.Value, Promise[ServerPacket]]) extends TestTarget[AuthClient] {

  var challenge: ServerLogonChallengeSuccess = _

  var pool: Map[AuthOpCodes.Value, ActorRef] = Map()
  AuthOpCodes.values foreach (op => pool = pool + (op -> system.actorOf(Consumer.props(promises(op)))))

  override def await(opCode: AuthOpCodes.Value): Future[ServerPacket] = {
    promises(opCode).future
  }
}


/**
  * From the server's challenge, computes a proof that confirms the identity of the client to the
  * server
  *
  * @param account the player's login and password
  */
class SendProof(account: AccountEntry) extends Operation[AuthClient] {

  /**
    * Execute the specific operation
    *
    * @param tcpClient the actor through which the client is able to communicate with the server
    */
  override def apply[B <: ServerPacket](tcpClient: ActorRef, future: Option[Future[B]]): Unit = {
    implicit val ec = ExecutionContext.Implicits.global
    future foreach (_ onComplete {
      case Success(ServerLogonChallenge(_, Some(challenge))) =>
        tcpClient ! writePacket(Srp6Client.computeProof(account, challenge))
      case Success(_: ServerLogonChallenge) => println("Challenge generation failed")
      case Success(p: ServerPacket) => println(s"Got an unexpected packet: $p")
      case Failure(t) => println(s"Something went wrong: $t")
    })
  }
}

/**
  * Sends a realmlist request
  */
class SendRealmlistRequest extends Operation[AuthClient] {

  /**
    * Execute the specific operation
    *
    * @param tcpClient the actor through which the client is able to communicate with the server
    */
  override def apply[B <: ServerPacket](tcpClient: ActorRef, future: Option[Future[B]] = None): Unit =
    tcpClient ! writePacket(ClientRealmlist())
}

/**
  * Sends a challenge
  *
  * @param ip    the client's ip
  * @param login the player's login
  */
class SendChallenge(ip: Vector[Int], login: String) extends Operation[AuthClient] {

  /**
    * Execute the specific operation
    *
    * @param tcpClient the actor through which the client is able to communicate with the server
    */
  override def apply[B <: ServerPacket](tcpClient: ActorRef, future: Option[Future[B]] = None): Unit =
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

