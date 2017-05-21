package wow.realm.world

import akka.actor.{Actor, ActorLogging, Props}
import wow.Application
import wow.realm.entities.{CharacterRef, Guid}
import wow.realm.events.{DispatchWorldUpdate, PlayerJoined, Tick, WorldEvent}
import wow.realm.world.WorldState.{GetState, State}
import wow.realm.{RealmContext, RealmContextData}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * World state manager
  */
class WorldState(override implicit val realm: RealmContextData) extends Actor with ActorLogging with RealmContext {
  private val scheduler = context.system.scheduler

  private val tickInterval = 50 milliseconds

  realm.eventStream.subscribe(self, classOf[PlayerJoined])
  realm.eventStream.subscribe(self, classOf[Tick])

  private var currentTick: Tick = Tick(0, Application.uptimeMillis(), Tick(0, 0, null))
  private val tickToken = scheduler.schedule(tickInterval, tickInterval,
    () => {
      val msTime = Application.uptimeMillis()
      val nextNumber = currentTick.number + 1
      val nextTick = Tick(nextNumber, msTime, currentTick)
      currentTick = nextTick

      realm.eventStream.publish(nextTick)
    })(context.dispatcher)


  override def postStop(): Unit = {
    tickToken.cancel()

    super.postStop()
  }

  private var events = mutable.MutableList[WorldEvent]()
  private val characters = mutable.HashMap[Guid.Id, CharacterRef]()

  override def receive: Receive = {
    case e@PlayerJoined(character) =>
      characters(character.guid.id) = character
      events += e

    case Tick(number, msTime, previousTick) if events.nonEmpty =>
      log.debug(s"Received tick $number at $msTime (diff ${msTime - previousTick.msTime})")
      realm.eventStream.publish(DispatchWorldUpdate(events))

      events = mutable.MutableList[WorldEvent]()

    case GetState =>
      sender ! State(characters.toMap.values)
  }
}

object WorldState {
  def props(implicit realm: RealmContextData): Props = Props(new WorldState)

  val PreferredName = "worldstate"

  sealed trait Event

  case object GetState extends Event

  case class State(characters: Iterable[CharacterRef]) extends Event

}

