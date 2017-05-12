package wow.common.network

import akka.actor.{ActorRef, Props}

/**
  * Contract for session actor companion object
  */
trait TCPSessionFactory {
  def props(connection: ActorRef): Props

  val PreferredName: String
}
