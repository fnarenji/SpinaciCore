package ensiwow.realm.handlers

import ensiwow.realm.entities.{Guid, GuidType, Position}
import ensiwow.realm.protocol.payloads._
import ensiwow.realm.protocol.{OpCodes, PayloadlessPacketHandler, PayloadlessPacketHandlerFactory}
import ensiwow.realm.session.NetworkWorker

/**
  * Created by sknz on 3/15/17.
  */
class CharEnumHandler extends PayloadlessPacketHandler {
  val mockCharEnum =  ServerCharEnum(
    Vector(
      ServerCharEnumEntry(
        Guid(8, GuidType.Player),
        CharInfo(
          "VagueNerf",
          race = 1,
          charClass = 4,
          gender = 0,
          skin = 8,
          face = 8,
          hairStyle = 7,
          hairColor = 6,
          facialHair = 4
        ),
        1,
        12,
        Position.mxyzo(0, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
        0,
        0x02000000,
        0,
        0,
        Pet(0, 0, 0)
      ),
      ServerCharEnumEntry(
        Guid(9, GuidType.Player),
        CharInfo(
          "BrocQue10",
          race = 1,
          charClass = 4,
          gender = 0,
          skin = 0,
          face = 6,
          hairStyle = 6,
          hairColor = 0,
          facialHair = 6
        ),
        1,
        12,
        Position.mxyzo(0, -8937.25488f, -125.310707f, 82.8524399f, 0.662107527f),
        0,
        0x02000000,
        0,
        0,
        Pet(0, 0, 0)
      )
    )
  )

  /**
    * Processes empty payload
    */
  override protected def process: Unit = {
    sender ! NetworkWorker.EventOutgoing(mockCharEnum)
  }

}

object CharEnumHandler extends PayloadlessPacketHandlerFactory[CharEnumHandler](OpCodes.CharEnum)
