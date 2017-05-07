package wow.realm.protocol.payloads

import wow.common.codecs._
import wow.common.{Expansions, VersionInfo}
import wow.realm.protocol._
import scodec.Codec
import scodec.codecs._

/**
  * Client's position in the realm's queue. Only happens if response code is WaitQueue.
  * @param position position
  * @param freeMigration is free character migration available from this realm
  */
case class ServerAuthResponseQueue(position: Long, freeMigration: Boolean)

object ServerAuthResponseQueue {
  implicit val codec: Codec[ServerAuthResponseQueue] = {
    ("position" | uint32L) ::
      ("freeMigration" | cbool(8))
  }.as[ServerAuthResponseQueue]
}

/**
  * Successful authentication response
  * @param queue queue info
  */
case class ServerAuthResponseSuccess(queue: Option[ServerAuthResponseQueue])

object ServerAuthResponseSuccess {
  implicit val codec: Codec[ServerAuthResponseSuccess] = {
    constantE(0L)(uint32L) ::
      constantE(0)(uint8L) ::
      constantE(0L)(uint32L) ::
      constantE(VersionInfo.SupportedExpansion)(enumerated(uint8L, Expansions)) ::
      ("queue" | notEmpty(Codec[ServerAuthResponseQueue]))
  }.as[ServerAuthResponseSuccess]
}

/**
  * Server auth response
  */
case class ServerAuthResponse(response: AuthResponses.Value,
                              success: Option[ServerAuthResponseSuccess]) extends Payload with ServerSide {
  private val successResponses = Array(AuthResponses.Ok, AuthResponses.WaitQueue)

  require(successResponses.contains(response) == success.nonEmpty)

  success foreach { _success =>
      require(response == AuthResponses.WaitQueue == _success.queue.nonEmpty)
  }
}

object ServerAuthResponse {
  implicit val opCodeProvider: OpCodeProvider[ServerAuthResponse] = OpCodes.SAuthResponse

  implicit val codec: Codec[ServerAuthResponse] = {
    ("authResult" | Codec[AuthResponses.Value]) >>:~ { response =>
      ("success" | conditional(response == AuthResponses.Ok, Codec[ServerAuthResponseSuccess])).hlist
    }
  }.as[ServerAuthResponse]
}
