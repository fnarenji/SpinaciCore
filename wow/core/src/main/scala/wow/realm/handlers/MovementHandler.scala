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
  extends PayloadHandler[NetworkWorker, ClientMovement](MovementHandlerCompanion.SupportedOpCodes) {
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

object MovementHandlerCompanion {
  val SupportedOpCodes: OpCodes.ValueSet = OpCodes.ValueSet(
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
}

