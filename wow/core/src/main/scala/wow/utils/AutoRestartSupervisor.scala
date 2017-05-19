package wow.utils

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

/**
  * Automatically restarts its child actor
  */
class AutoRestartSupervisor(childProps: Props, childName: String) extends Actor with ActorLogging {
  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  context.actorOf(childProps, childName)

  override def receive: Receive = PartialFunction.empty
}
