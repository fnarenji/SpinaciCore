package ensiwow.realm.world

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.realm.entities.{CharacterRef, Guid}
import ensiwow.realm.events.{DispatchWorldUpdate, PlayerJoined, Tick, WorldEvent}
import ensiwow.realm.world.WorldState.{GetState, State}

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * World state manager
  */
class WorldState extends Actor with ActorLogging {
  private val eventStream = context.system.eventStream
  private val scheduler = context.system.scheduler

  private val tickInterval = 50 milliseconds

  eventStream.subscribe(self, classOf[PlayerJoined])
  eventStream.subscribe(self, classOf[Tick])
  eventStream.publish(Tick(0, Application.uptimeMillis(), Tick(0, 0, null)))

  private var events = mutable.MutableList[WorldEvent]()
  private val characters = mutable.HashMap[Guid.Id, CharacterRef]()

  override def receive: Receive = {
    case e@PlayerJoined(character) =>
      characters(character.guid.id) = character
      events += e

    case currentTick@Tick(number, msTime, previousTick) =>
      if (events.nonEmpty) {
        log.debug(s"Received tick $number at $msTime (diff ${msTime - previousTick.msTime})")
        eventStream.publish(DispatchWorldUpdate(events))

        events = mutable.MutableList[WorldEvent]()
      }

      scheduler.scheduleOnce(tickInterval,
        () => {
          val msTime = Application.uptimeMillis()
          val nextNumber = currentTick.number + 1
          val nextTick = Tick(nextNumber, msTime, currentTick)

          eventStream.publish(nextTick)
        })(context.dispatcher)

    case GetState =>
      sender ! State(characters.toMap.values)
  }
}

object WorldState {
  def props: Props = Props(classOf[WorldState])

  val PreferredName = "WorldState"

  sealed trait Event

  object GetState extends Event

  case class State(characters: Iterable[CharacterRef]) extends Event
}

