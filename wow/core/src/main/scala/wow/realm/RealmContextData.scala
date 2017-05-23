package wow.realm

import akka.actor.ActorRef
import akka.event.EventStream
import wow.realm.entities.CharacterInfoDao

/**
  * Contextual information for actors within a realm
  */
case class RealmContextData(id: Int, eventStream: EventStream, serverRef: ActorRef, characterDao: CharacterInfoDao)

trait RealmContext {
  implicit val realm: RealmContextData
}

