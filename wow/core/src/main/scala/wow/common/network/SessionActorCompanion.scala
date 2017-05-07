package wow.common.network

import akka.actor.Props

/**
  * Contract for session actor companion object
  */
trait SessionActorCompanion {
  def props: Props

  def PreferredName: String
}
