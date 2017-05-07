package wow.realm.handlers

import wow.Application
import wow.realm.events.PlayerMoved
import wow.realm.protocol.payloads.ClientMovement
import wow.realm.protocol.{MultiPayloadHandlerFactory, OpCodes, PacketSerialization, PayloadHandler}

/**
  * Movement packet handler
  */
class MovementHandler extends PayloadHandler[ClientMovement] {
  private val eventStream = context.system.eventStream

  /**
    * Processes an incoming payload
    *
    * @param payload payload to be processed
    */
  override protected def process(payload: ClientMovement): Unit = {
    val clientDelay = Application.uptimeMillis() - payload.time

    val adjustedPayload = payload.copy(time = payload.time + clientDelay)

    val (headerBits, payloadBits) = PacketSerialization.outgoingSplit(adjustedPayload, currentOpCode)

    eventStream.publish(PlayerMoved(payload, headerBits, payloadBits))

    log.debug(s"Player ${payload.guid.id} moved to ${payload.position} with $currentOpCode")
  }
}

object MovementHandler extends MultiPayloadHandlerFactory[MovementHandler, ClientMovement](
  OpCodes.MsgMoveStartForward,
  OpCodes.MsgMoveStartBackward,
  OpCodes.MsgMoveStop,
  OpCodes.MsgMoveStartStrafeLeft,
  OpCodes.MsgMoveStartStrafeRight,
  OpCodes.MsgMoveStopStrafe,
  OpCodes.MsgMoveJump,
  OpCodes.MsgMoveStartTurnLeft,
  OpCodes.MsgMoveStartTurnRight,
  OpCodes.MsgMoveStopTurn,
  OpCodes.MsgMoveStartPitchUp,
  OpCodes.MsgMoveStartPitchDown,
  OpCodes.MsgMoveStopPitch,
  OpCodes.MsgMoveSetRunMode,
  OpCodes.MsgMoveSetWalkMode,
  OpCodes.MsgMoveFallLand,
  OpCodes.MsgMoveStartSwim,
  OpCodes.MsgMoveStopSwim,
  OpCodes.MsgMoveSetFacing,
  OpCodes.MsgMoveSetPitch,
  OpCodes.MsgMoveHeartbeat,
  OpCodes.MoveFallReset,
  OpCodes.MoveSetFly,
  OpCodes.MsgMoveStartAscend,
  OpCodes.MsgMoveStopAscend,
  OpCodes.MoveChangeTransport,
  OpCodes.MsgMoveStartDescend
)

