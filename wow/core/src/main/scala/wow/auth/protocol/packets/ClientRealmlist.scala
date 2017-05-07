package wow.auth.protocol.packets

import wow.common.codecs._
import wow.auth.protocol.{ClientPacket, OpCodes}
import scodec._
import scodec.codecs._

import scala.language.postfixOps

/**
  * A client realmlist request contains only four null bytes.
  */
case class ClientRealmlist() extends ClientPacket

/**
  * The client realmlist request descriptor.
  */
object ClientRealmlist {
  implicit val codec: Codec[ClientRealmlist] = {
    constantE(OpCodes.RealmList) ::
      constantE(0L)(uint32L)
  }.as[ClientRealmlist]
}
