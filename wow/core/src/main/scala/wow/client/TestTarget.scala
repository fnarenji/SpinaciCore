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

trait TestTarget[A <: TestTarget[A]] {
  def execute(operations: List[Operation[A]], tcpClient: ActorRef): Unit = {
    operations foreach (o => o(tcpClient))
  }

  def execute(o: Operation[A], tcpClient: ActorRef): Unit = {
    o(tcpClient)
  }

  def await(opCode: OpCodes.Value): Future[ServerPacket]

  var buffer: ParVector[BitVector] = new ParVector
}

trait Operation[A <: TestTarget[A]] {
  def apply(tcpClient: ActorRef): Unit

  def writePacket[B <: ClientPacket](packet: B)(implicit codec: Codec[B]): Write = {
    val bits: ByteString = PacketSerializer.serialize(packet)(codec).bytes.toByteString
    Write(bits)
  }
}
