package wow.realm.session

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import wow.common.database.AsyncDB
import wow.realm.entities.{CharacterDAO, Guid}
import wow.realm.events.{DispatchWorldUpdate, PlayerJoined, PlayerMoved}
import wow.realm.protocol._
import wow.realm.protocol.payloads.{ServerLoginVerifyWorld, ServerTimeSyncRequest, ServerUpdateBlock,
ServerUpdateObject}
import wow.realm.world.WorldState
import wow.realm.{RealmContext, RealmContextData, RealmServer}

import scala.concurrent.duration._

/**
  * Represents a Session's current character
  *
  * @param guid          guid of character
  * @param networkWorker network worker associated to session
  */
class Character(guid: Guid, override val networkWorker: ActorRef)(override implicit val realm: RealmContextData)
  extends Actor
          with ActorLogging
          with RealmContext
          with PacketHandlerTag
          with ForwardToNetworkWorker {

  import context.dispatcher

  private val scheduler = context.system.scheduler

  private val worldState = context.actorSelection(RealmServer.WorldStatePath)
  private var updateBlocks = Vector.newBuilder[ServerUpdateBlock]

  private val currentCharacter = CharacterDAO.findByGuid(guid).getOrElse(
    throw new IllegalStateException(s"Character $guid not found in database, but player still selected it"))

  val loginVerifyWorld = ServerLoginVerifyWorld(currentCharacter.position)
  sendPayload(loginVerifyWorld)

  realm.eventStream.subscribe(self, classOf[DispatchWorldUpdate])
  realm.eventStream.subscribe(self, classOf[PlayerMoved])

  case object SaveCharacterToDatabase

  private val timeSyncSenderToken = scheduler.schedule(1 second, 10 seconds, TimeSyncRequestSender)
  private val databaseSaveToken = scheduler.schedule(60 seconds, 60 seconds, self, SaveCharacterToDatabase)

  realm.eventStream.publish(PlayerJoined(currentCharacter.ref))

  worldState ! WorldState.GetState

  override def postStop(): Unit = {
    timeSyncSenderToken.cancel()
    databaseSaveToken.cancel()

    CharacterDAO.save(currentCharacter)

    super.postStop()
  }

  override def receive: Receive = {
    case WorldState.State(characters) =>
      for (character <- characters) {
        if (character.guid != currentCharacter.guid) {
          val block = ObjectUpdateBlockHelpers.createCharacter(character, isSelf = false)

          updateBlocks += block
        }
      }

    case DispatchWorldUpdate(events) =>
      events.collect {
        case PlayerJoined(charView) =>
          val block = ObjectUpdateBlockHelpers.createCharacter(charView, currentCharacter.guid == charView.guid)

          updateBlocks += block
      }

      val updateObject = ServerUpdateObject(updateBlocks.result())
      sendPayload(updateObject)

      updateBlocks = Vector.newBuilder[ServerUpdateBlock]

    case PlayerMoved(payload, headerBits, payloadBits) =>
      payload.guid match {
        case currentCharacter.guid =>
          currentCharacter.position = payload.position
        case _ =>
          sendRaw(headerBits, payloadBits)
      }

    case SaveCharacterToDatabase =>
      val copy = currentCharacter.copy()
      AsyncDB {
        CharacterDAO.save(copy)
      }
  }

  private object TimeSyncRequestSender extends Runnable {
    private var count = 0L

    override def run(): Unit = {
      val timeSyncRequest = ServerTimeSyncRequest(count)
      count = count + 1

      sendPayload(timeSyncRequest)
    }
  }
}

object Character {
  def props(guid: Guid, networkWorker: ActorRef)(implicit realm: RealmContextData): Props =
    Props(new Character(guid, networkWorker)(realm))

  def PreferredName(guid: Guid) = s"character-${guid.id}"
}

