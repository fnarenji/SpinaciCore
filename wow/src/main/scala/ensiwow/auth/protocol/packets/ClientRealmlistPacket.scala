package ensiwow.auth.protocol.packets

import ensiwow.common.codecs._
import ensiwow.auth.protocol.{ClientPacket, OpCodes}
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * A client realmlist request contains only four null bytes.
  */
case class ClientRealmlistPacket() extends ClientPacket

/**
  * The client realmlist request descriptor.
  */
object ClientRealmlistPacket {
  implicit val codec: Codec[ClientRealmlistPacket] = {
    constantE(OpCodes.RealmList) ::
      constantE(0L)(uint32L)
  }.as[ClientRealmlistPacket]
}
