package ensiwow.realm.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.common.network.{EventPacket, OutgoingPacket, Session}
import ensiwow.realm.protocol.PacketBuilder
import ensiwow.realm.protocol.payloads.ServerAuthChallenge

import scala.language.postfixOps
import scala.util.Random

/**
  * Handles a realm session
  */
class RealmSession extends Actor with ActorLogging {
  {
    val authChallenge = PacketBuilder.server(
      ServerAuthChallenge(
        ThreadLocalRandom.current().nextLong(0xFFFFFFFF1L),
        BigInt(ServerAuthChallenge.SeedSize, Random),
        BigInt(ServerAuthChallenge.SeedSize, Random)))

    context.parent ! OutgoingPacket(authChallenge)
  }

  override def receive = {
    case EventPacket(bits) =>
      log.debug(s"Got packet: ${bits.toHex}")
  }
}

object RealmSession extends Session {
  override def props: Props = Props(classOf[RealmSession])

  override def PreferredName = "RealmSession"
}
