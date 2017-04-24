package ensiwow.realm.protocol.payloads

import ensiwow.realm.protocol._
import scodec.Codec
import scodec.codecs._

import scala.language.postfixOps

case class ClientCharCreate(createInfo: CreateInfo) extends Payload[ClientHeader]

object ClientCharCreate {
  implicit val opCodeProvider: OpCodeProvider[ClientCharCreate] = OpCodes.CharCreate

  implicit val codec: Codec[ClientCharCreate] = ("createInfo" | Codec[CreateInfo]).as[ClientCharCreate]
}

case class ServerCharCreate(responseCode: Int) extends Payload[ServerHeader]

object ServerCharCreate {
  implicit val opCodeProvider: OpCodeProvider[ServerCharCreate] = OpCodes.SCharCreate

  implicit val codec: Codec[ServerCharCreate] = ("responseCode" | uint8L).as[ServerCharCreate]
}

