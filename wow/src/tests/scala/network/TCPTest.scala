package network

import ensiwow.auth.network.TCPServer
import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yanncolina on 08/02/17.
  * Class which aims at testing the comunications between a client and a TCP server
  */
class TCPTest extends FlatSpec with Matchers {
    val system = ActorSystem("tcpTests")
    val server = system.actorOf(Props[TCPServer], "server")
    val client = system.actorOf(Client.props(new InetSocketAddress("localhost", 5555), server), "client")

    implicit val timeout = Timeout(Duration(5, SECONDS))

    system.terminate()
}
