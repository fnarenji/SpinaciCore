package ensiwow.realm.protocol.payloads

import ensiwow.common.codecs._
import ensiwow.common.{Expansions, VersionInfo}
import ensiwow.realm.protocol._
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
                              success: Option[ServerAuthResponseSuccess]) extends Payload[ServerHeader] {
  private val successResponses = Array(AuthResponses.Ok, AuthResponses.WaitQueue)

  require(successResponses.contains(response) == success.nonEmpty)

  success match {
    case Some(success) =>
      require(response == AuthResponses.WaitQueue == success.queue.nonEmpty)
    case None =>
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
