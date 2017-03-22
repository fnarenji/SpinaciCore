package ensiwow.realm.protocol.packets

import ensiwow.realm.protocol._
import ensiwow.common.codecs._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Created by yanncolina on 17/03/17.
  */
case class ClientPingPacket(ping: Long, latency: Long) extends Payload[ClientHeader]

object ClientPingPacket {
  implicit val opCodeProvider: OpCodeProvider[ClientPingPacket] = OpCodes.Ping

  implicit val codec: Codec[ClientPingPacket] = {
    constantE(OpCodes.Ping) ::
      ("ping" | uint32L) ::
      ("latency" | uint32L)
  }.as[ClientPingPacket]
}

case class ServerPongPacket(ping: Long) extends Payload[ServerHeader]

object ServerPongPacket {
  implicit val opCodeProvider: OpCodeProvider[ServerPongPacket] = OpCodes.SPong

  implicit val codec: Codec[ServerPongPacket] = {
    constantE(OpCodes.SPong) ::
      ("ping" | uint32L)
  }.as[ServerPongPacket]
}
