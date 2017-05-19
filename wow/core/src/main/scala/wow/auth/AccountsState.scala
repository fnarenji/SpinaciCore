package wow.auth

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import wow.Application
import wow.auth.AccountsState.{AccountIdentifier, IsOnline, NotifyAccountOnline}

import scala.collection.mutable

/**
  * Tracks online state for every account
  */
class AccountsState extends Actor with ActorLogging {
  /**
    * Map of actor ref representing account (NetworkWorker) by account identifier (login -> NetworkWorker ref)
    */
  private val accountRefById = new mutable.HashMap[AccountIdentifier, ActorRef]()

  /**
    * Map of account identifier by actor ref  (NetworkWorker ref -> login)
    */
  private val accountByActor = new mutable.HashMap[ActorRef, AccountIdentifier]()

  override def receive: Receive = {
    case NotifyAccountOnline(id, networkWorker) =>
      accountRefById(id) = networkWorker
      accountByActor(networkWorker) = id
      context.watch(networkWorker)

    case Terminated(subject) =>
      accountByActor.remove(subject).foreach(id => accountRefById.remove(id))

    case IsOnline(id) =>
      sender ! accountRefById.contains(id)
  }
}

object AccountsState {
  type AccountIdentifier = String

  def props: Props = Props(new AccountsState)

  val PreferredName = "AccountsState"
  val ActorPath = s"${Application.ActorPath}/$PreferredName"

  /**
    * Marks an account as online and ties its online state to the NetworkWorker
    *
    * @param id            account id
    * @param networkWorker associated network worker
    */
  case class NotifyAccountOnline(id: AccountIdentifier, networkWorker: ActorRef)

  /**
    * Asks if an account is online.
    *
    * @param id account identifier
    */
  case class IsOnline(id: AccountIdentifier)
}

