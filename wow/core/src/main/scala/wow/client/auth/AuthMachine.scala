package wow.client.auth

import akka.io.Tcp.Write
import akka.util.ByteString
import scodec.Codec
import scodec.bits.BitVector
import scodec.interop.akka._
import wow.auth.protocol.packets._
import wow.auth.protocol.ClientPacket

/**
  * Every state of the authentication machine is defined by its state and a resulting generated
  * packet to be sent through the socket
  * @param state a state of the automaton
  * @param data an object to be sent through the socket
  */
abstract class FsmState(val state: AuthState.Value, val data: Option[Write])

/**
  * An enumeration that defines the state of the authentication automaton
  */
object AuthState extends Enumeration {
  type State = Value
  val Authenticated, Challenge, Proof, NoState = Value
}

/**
  * The common trait of the events that will trigger the automaton transitions
  */
trait Event

case class EventAuthenticate() extends Event

case class EventIncoming(bits: BitVector) extends Event

case class AuthMachine(authState: AuthState.State, outgoingData: Option[Write])
  extends FsmState(state = authState, data = outgoingData)

/**
  * The companion object that defines the behaviour of the automaton
  */
object AuthMachine {

  private def writePacket[A <: ClientPacket](packet: A)(implicit codec: Codec[A]): Write = {
    val bits: ByteString = PacketSerializer.serialize(packet)(codec).bytes.toByteString
    Write(bits)
  }

  /**
    * Clones the machine with new attributes
    * @param a the automaton to be cloned
    * @param s the new state
    * @param p eventually, define an attribute to be sent through the socket
    * @return an automaton with these new attributes
    */
  private def cloneNewState(a: AuthMachine, s: AuthState.State, p: Option[Write]): AuthMachine =
    a.copy(authState = s, outgoingData = p)

  /**
    * This method defines how the machine must react to an event
    * @param a the authentication machine
    * @param e an event
    * @return a copy of the machine
    */
  def transition(a: AuthMachine, e: Event): AuthMachine = {
    a.authState match {
      case AuthState.NoState =>
        e match {
          case EventAuthenticate() =>
            cloneNewState(a, AuthState.Challenge, Some(writePacket(Srp6Client.challengeRequest)(ClientChallenge.logonChallengeCodec)))
        }
      case AuthState.Challenge =>
        e match {
          case EventIncoming(bits) =>
            val packet: ServerLogonChallenge =
              PacketSerializer.deserialize[ServerLogonChallenge](bits)(ServerLogonChallenge.codec)
            packet.success match {
              case Some(challenge) =>
                cloneNewState(a, AuthState.Proof, Some(writePacket(Srp6Client.computeProof(challenge))))
              case None =>
                println("Challenge generation seems to have failed")
                a
            }
        }
      case AuthState.Proof =>
        e match {
          case EventIncoming(bits) =>
            PacketSerializer.deserialize[ServerLogonProof](bits)(ServerLogonProof.codec) match {
              case ServerLogonProof(_, Some(_), _) =>
                println("Authentication success")
                cloneNewState(a, AuthState.Authenticated, Some(writePacket(ClientRealmlist())))
              case ServerLogonProof(_, None, _) =>
                println("Authentication failed")
                a
            }
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
