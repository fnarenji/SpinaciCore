package wow.realm

import akka.actor.ActorRef
import akka.event.EventStream
import wow.realm.objects.characters.CharacterDao

/**
  * Contextual information for actors within a realm
  */
case class RealmContextData(id: Int, eventStream: EventStream, serverRef: ActorRef, characterDao: CharacterDao)

trait RealmContext {
  implicit val realm: RealmContextData
}

