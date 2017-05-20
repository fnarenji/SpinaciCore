package wow.client.auth

import scodec.bits.BitVector
import wow.auth.protocol.packets._
import wow.auth.protocol.ClientPacket


abstract class FsmState[A <: ClientPacket](val state: AuthState.Value, val data: Option[A])

object AuthState extends Enumeration {
  type State = Value
  val Authenticated, Challenge, Proof, NoState = Value
}

trait Event

case class EventAuthenticate() extends Event

case class EventIncoming(bits: BitVector) extends Event

case class AuthMachine(authState: AuthState.State, outgoingData: Option[ClientPacket])
  extends FsmState(state = authState, data = outgoingData)

object AuthMachine {

  private def cloneNewState[A <: ClientPacket](a: AuthMachine, s: AuthState.State, p: Option[A]) =
    a.copy(authState = s, outgoingData = p)

  def transition(a: AuthMachine, e: Event): AuthMachine = {
    a.authState match {
      case AuthState.NoState =>
        e match {
          case EventAuthenticate() =>
            cloneNewState(a, AuthState.Challenge, Some(SRP6.challengeRequest))
        }
      case AuthState.Challenge =>
        e match {
          case EventIncoming(bits) =>
            val packet: ServerLogonChallenge =
              PacketSerializer.deserialize[ServerLogonChallenge](bits)(ServerLogonChallenge.codec)
            packet.success match {
              case Some(challenge) => cloneNewState(a, AuthState.Proof, Some(SRP6.computeProof(challenge)))
            }
        }
      case AuthState.Proof =>
        e match {
          case EventIncoming(bits) =>
            val packet: ServerLogonProof = PacketSerializer.deserialize[ServerLogonProof](bits)(ServerLogonProof.codec)
            println(s"Got server's proof: $packet")
            cloneNewState(a, AuthState.Authenticated, Some(ClientRealmlist()))
        }
      case AuthState.Authenticated =>
        e match {
          case EventIncoming(bits) =>
            val packet: ServerRealmlist = PacketSerializer.deserialize[ServerRealmlist](bits)(ServerRealmlist.codec)
            println("Authenticated, the available realms are: ")
            packet.realms foreach (realm => println(realm.name))
            cloneNewState(a, AuthState.Authenticated, None)
        }
    }
  }
}
