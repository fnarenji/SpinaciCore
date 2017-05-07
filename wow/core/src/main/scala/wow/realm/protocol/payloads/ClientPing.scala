package wow.realm.protocol.payloads

import wow.realm.protocol._
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * Created by yanncolina on 17/03/17.
  */
case class ClientPing(ping: Long, latency: Long) extends Payload with ClientSide

object ClientPing {
  implicit val opCodeProvider: OpCodeProvider[ClientPing] = OpCodes.Ping

  implicit val codec: Codec[ClientPing] = {
      ("ping" | uint32L) ::
      ("latency" | uint32L)
  }.as[ClientPing]
}

case class ServerPong(ping: Long) extends Payload with ServerSide

object ServerPong {
  implicit val opCodeProvider: OpCodeProvider[ServerPong] = OpCodes.SPong

  implicit val codec: Codec[ServerPong] = ("ping" | uint32L).as[ServerPong]
}
