package ensiwow.realm.session

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.common.network.{EventPacket, OutgoingPacket, Session}
import ensiwow.realm.protocol.PacketSerialization
import ensiwow.realm.protocol.payloads.ServerAuthChallenge

import scala.language.postfixOps
import scala.util.Random

/**
  * Handles a realm session
  */
class RealmSession extends Actor with ActorLogging {
  // Send initial packet
  {
    val SeedSizeBits = ServerAuthChallenge.SeedSize * 8

    val authChallenge = PacketSerialization.server(
      ServerAuthChallenge(
        ThreadLocalRandom.current().nextLong(0x7FFFFFFFL),
        BigInt(SeedSizeBits, Random),
        BigInt(SeedSizeBits, Random)))

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
