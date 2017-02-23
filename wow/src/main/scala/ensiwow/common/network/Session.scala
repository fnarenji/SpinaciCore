package ensiwow.common.network

import akka.actor.Props

/**
  * Created by sknz on 2/23/17.
  */
trait Session {
  def props: Props

  def PreferredName: String
}
