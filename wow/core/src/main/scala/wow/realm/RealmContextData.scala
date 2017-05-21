package wow.realm

import akka.actor.ActorRef
import akka.event.EventStream
import wow.realm.entities.CharacterInfoDAO

/**
  * Contextual information for actors within a realm
  */
case class RealmContextData(id: Int, eventStream: EventStream, serverRef: ActorRef, characterDAO: CharacterInfoDAO)

trait RealmContext {
  implicit val realm: RealmContextData
}

