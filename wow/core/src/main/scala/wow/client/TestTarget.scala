package wow.client

import akka.actor.ActorRef
import akka.io.Tcp.Write
import akka.util.ByteString

import scodec.interop.akka._
import scodec.Codec
import scodec.bits.BitVector

import wow.auth.protocol.{ClientPacket, OpCodes, ServerPacket}
import wow.client.auth.PacketSerializer

import scala.collection.parallel.immutable.ParVector
import scala.concurrent.Future

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
  def execute(operations: List[Operation[A]], tcpClient: ActorRef): Unit = {
    operations foreach (o => o(tcpClient))
  }

  /**
    * Executes a single operation
    * @param o the operation to be executed
    * @param tcpClient the tcp client through which the client communicates with the server
    */
  def execute(o: Operation[A], tcpClient: ActorRef): Unit = {
    o(tcpClient)
  }

  /**
    * When triggered, the method waits for a specific type of packet
    * @param opCode it defines the type of packet
    * @return a future of the received packet
    */
  def await(opCode: OpCodes.Value): Future[ServerPacket]

  /**
    * stores the incoming packets
    */
  var buffer: ParVector[BitVector] = new ParVector
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
  def apply(tcpClient: ActorRef): Unit

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
