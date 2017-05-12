package wow.realm.handlers

import wow.Application
import wow.realm.events.PlayerMoved
import wow.realm.protocol._
import wow.realm.protocol.payloads.ClientMovement
import wow.realm.session.NetworkWorker

/**
  * Movement packet handler
  *
  * Uses OpCodeProvider.None as OpCodes are overloaded below
  */
object MovementHandler
  extends PayloadHandler[NetworkWorker, ClientMovement]({
    import OpCodes._

    OpCodes.ValueSet(
      MsgMoveStartForward,
      MsgMoveStartBackward,
      MsgMoveStop,
      MsgMoveStartStrafeLeft,
      MsgMoveStartStrafeRight,
      MsgMoveStopStrafe,
      MsgMoveJump,
      MsgMoveStartTurnLeft,
      MsgMoveStartTurnRight,
      MsgMoveStopTurn,
      MsgMoveStartPitchUp,
      MsgMoveStartPitchDown,
      MsgMoveStopPitch,
      MsgMoveSetRunMode,
      MsgMoveSetWalkMode,
      MsgMoveFallLand,
      MsgMoveStartSwim,
      MsgMoveStopSwim,
      MsgMoveSetFacing,
      MsgMoveSetPitch,
      MsgMoveHeartbeat,
      MoveFallReset,
      MoveSetFly,
      MsgMoveStartAscend,
      MsgMoveStopAscend,
      MoveChangeTransport,
      MsgMoveStartDescend
    )
  }) {

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  override protected def handle(header: ClientHeader, payload: ClientMovement)(self: NetworkWorker): Unit = {
    import self._

    val clientDelay = Application.uptimeMillis() - payload.time

    val adjustedPayload = payload.copy(time = payload.time + clientDelay)

    val (headerBits, payloadBits) = PacketSerialization.outgoingSplit(adjustedPayload, header.opCode)

    realm.eventStream.publish(PlayerMoved(payload, headerBits, payloadBits))

    log.debug(s"Player ${payload.guid.id} moved to ${payload.position} with ${header.opCode}")
  }
}

