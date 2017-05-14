package wow.visualizer

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch._
import akka.event.EventStream
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import wow.api.API

class FloFloStream(sys: ActorSystem, private val debug: Boolean) extends EventStream(sys, debug) {
  def publishVisualize(event: AnyRef)(who: ActorRef): Unit = {
    if (event.getClass.getName.startsWith("wow.")) {
      VisualizerAPI.msg +=
        VisualMailboxMetric(who.path.toStringWithoutAddress, "/deadLetters", event.hashCode(), event.toString)
    }
    publish(event)
  }

  override protected def publish(event: AnyRef, subscriber: ActorRef): Unit = {
    super.publish(event, subscriber)
  }
}

object VisualizerAPI extends API {
  val msg = scala.collection.parallel.mutable.ParHashSet[VisualMailboxMetric]()

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  implicit val metricFormat: RootJsonFormat[Exch] = jsonFormat5(Exch)
  implicit val msgFormat: RootJsonFormat[ExchList] = jsonFormat1(ExchList)

  case class Exch(sender: String, receiver: Seq[String], bus: Boolean, message: String, time: Long)

  case class ExchList(exchs: Seq[Exch])

  override val route: Route = pathPrefix("visualizer") {
    path("get") {
      get {
        pathEnd {
          val order = new Ordering[VisualMailboxMetric] {
            override def compare(
              x: VisualMailboxMetric,
              y: VisualMailboxMetric): Int = x.measureTimeMs.compareTo(y.measureTimeMs)
          }

          val v = msg.toVector
          val groupByMessage = v.groupBy(m => m.hash).values.toStream
          val orderByTime = groupByMessage.sortBy(m => m.min(order).measureTimeMs)
          val exchs = orderByTime.map { m =>
            val sender = m.map(_.sender).collectFirst { case x if !x.contentEquals("/deadLetters") && !x.startsWith("/temp") => x }
              .getOrElse("/Broken/This does not work")
            val receivers = m.map(_.receiver).filterNot(x => x.contentEquals("/deadLetters") || x.startsWith("/temp"))
            val bus = !m
              .forall(x => !x.receiver.contentEquals("/deadLetters") && !x.sender.contentEquals("/temp"))

            assert(m.forall(_.message.contentEquals(m(0).message)))
            Exch(sender, receivers, bus, m(0).message, m(0).measureTimeMs)
          }

          complete(ExchList(exchs))
        }
      }
    }
  }
}

case class VisualMailboxMetric(
  sender: String,
  receiver: String,
  hash: Int,
  message: String,
  measureTimeMs: Long = System.currentTimeMillis())

class VisualMailboxType() extends MailboxType with ProducesMessageQueue[VisualMailbox] {

  def this(settings: ActorSystem.Settings, config: Config) = this()

  final override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue =
    new VisualMailbox(UnboundedMailbox().create(owner, system), owner, system)
        with UnboundedMessageQueueSemantics
        with MultipleConsumerSemantics
}

class VisualMailbox(val backend: MessageQueue, owner: Option[ActorRef], system: Option[ActorSystem])
  extends MessageQueue {

  override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    val name = handle.message.getClass.getName
    if (name.startsWith("wow.")) {
      val address = handle.sender.path.toStringWithoutAddress
      val metric = VisualMailboxMetric(
        address,
        receiver.path.toStringWithoutAddress,
        handle.message.hashCode(),
        handle.message.toString
      )
      VisualizerAPI.msg += metric
    } else {
      system.get.log.debug(s"SKIPPPPP $name")
    }

    backend.enqueue(receiver, handle)
  }

  override def dequeue(): Envelope = backend.dequeue()

  override def numberOfMessages: Int = backend.numberOfMessages

  override def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit = backend.cleanUp(owner, deadLetters)

  override def hasMessages: Boolean = backend.hasMessages
}


