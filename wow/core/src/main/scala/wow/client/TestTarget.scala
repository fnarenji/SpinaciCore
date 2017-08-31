package wow.client

import akka.actor.ActorRef
import akka.io.Tcp.Write
import akka.util.ByteString
import scodec.interop.akka._
import scodec.Codec
import wow.auth.protocol.{ClientPacket, ServerPacket}
import wow.client.auth.{AuthOpCodes, PacketSerializer}

import scala.concurrent.{Future, Promise}

/**
  * A tested entity, such as the authentication client or the world client has
  * the ability to execute a list of elementary operations and await for specific
  * packets
  *
  * @tparam A the methods that execute operations, must only accept those which have been
  *           defined for a specific subclass of TestTarget
  */
trait TestTarget[A <: TestTarget[A]] {

  /**
    * Executes all the operations provided
    *
    * @param operations the list of operations
    * @param tcpClient  the tcp client through which the client communicates with the server
    */
  def execute(operations: List[(Operation[A], Promise[ServerPacket])], tcpClient: ActorRef): Unit = {
    var tmp: Option[Future[ServerPacket]] = None
    for ((o, p) <- operations) {
      o(tcpClient, tmp)
      tmp = Some(p.future)
    }
  }

  /**
    * Executes a single operation
    * @param o the operation to be executed
    * @param tcpClient the tcp client through which the client communicates with the server
    */
  def execute[B <: ServerPacket](o: Operation[A], tcpClient: ActorRef, prevPacket: Option[Future[B]] = None): Unit = {
    o(tcpClient, prevPacket)
  }

  /**
    * When triggered, the method waits for a specific type of packet
    * @return a future of the received packet
    */
  def await(opCode: AuthOpCodes.Value): Future[ServerPacket]

}

/**
  * The trait that define the elementary operations
  * @tparam A a concrete operation is defined for a specific subclass of TestTarget
  */
trait Operation[A <: TestTarget[A]] {
  /**
    * Execute the specific operation
    * @param tcpClient the actor through which the client is able to communicate with the server
    */
  def apply[B <: ServerPacket](tcpClient: ActorRef, future: Option[Future[B]]): Unit

  /**
    * From a client packet, it generates an object that can directly be sent to the server
    * @param packet the client packet
    * @param codec the codec which describes how to encode the packet
    * @tparam B the only packets that are supported by this method are the client packets
    * @return an object Write
    */
  protected def writePacket[B <: ClientPacket](packet: B)(implicit codec: Codec[B]): Write = {
    val bits: ByteString = PacketSerializer.serialize(packet)(codec).bytes.toByteString
    Write(bits)
  }
}
