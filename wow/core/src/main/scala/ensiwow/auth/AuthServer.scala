package ensiwow.auth

import akka.actor.{Actor, ActorLogging, Props}
import ensiwow.Application
import ensiwow.auth.crypto.Srp6Protocol
import ensiwow.auth.data.Account
import ensiwow.auth.handlers.{LogonChallengeHandler, LogonProofHandler, ReconnectChallengeHandler,
ReconnectProofHandler}
import ensiwow.auth.protocol.packets.{ServerRealmlist, ServerRealmlistEntry}
import ensiwow.auth.session.{AuthSession, EventRealmlist}
import ensiwow.auth.utils.PacketSerializer
import ensiwow.common.VersionInfo
import ensiwow.common.database.Databases
import ensiwow.common.network.TCPServer
import scalikejdbc._

case object GetRealmlist

/**
  * AuthServer is the base actor for all services provided by the authentication server.
  *
  * This actor is unique (e.g. singleton) per ActorSystem.
  * It holds ownership of the stateless packet handlers.
  */
class AuthServer extends Actor with ActorLogging {
  val address = "127.0.0.1"
  val port = 3724

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  initializeDatabase()

  // TODO: should handlers be put in a pool so that they scale accordingly to the load ?
  context.actorOf(LogonChallengeHandler.props, LogonChallengeHandler.PreferredName)
  context.actorOf(LogonProofHandler.props, LogonProofHandler.PreferredName)
  context.actorOf(ReconnectChallengeHandler.props, ReconnectChallengeHandler.PreferredName)
  context.actorOf(ReconnectProofHandler.props, ReconnectProofHandler.PreferredName)
  context.actorOf(TCPServer.props(AuthSession, address, port), TCPServer.PreferredName)

  // TODO: find a way to retrieve address and port
  private val realms = Vector(ServerRealmlistEntry(1, 0, 0, "EnsiWoW", "127.0.0.1:8085", 0, 1, 1, 1))
  private val serverRealmlistPacket: ServerRealmlist = ServerRealmlist(realms)
  private val serverRealmlistPacketBits = PacketSerializer.serialize(serverRealmlistPacket)
  private val eventRealmlist: EventRealmlist = EventRealmlist(serverRealmlistPacketBits)

  override def receive: PartialFunction[Any, Unit] = {
    case GetRealmlist => sender() ! eventRealmlist
  }

  /**
    * Creates connection to database and sets it up if necessary
    */
  private def initializeDatabase(): Unit = {
    ConnectionPool.add(Databases.AuthServer,
      "jdbc:postgresql://localhost:5432/ensiwow_auth?currentSchema=ensiwow", "ensiwow", "")

    createSchema()

    def createSchema() = {
      val createDatabase = NamedDB(Databases.AuthServer) readOnly { implicit session =>
        val tableCount =
          sql"""
            select count(*) as count
            from information_schema.tables
            where table_name = 'account'
          """
            .map(_.int("count"))
            .single()
            .apply()
            .get

        tableCount == 0
      }

      // TODO: replace this with migration management
      if (createDatabase) {
        NamedDB(Databases.AuthServer) autoCommit { implicit session =>
          sql"drop table if exists account".execute().apply()
          sql"""
            create table account
            (
              id serial8 primary key,
              login varchar(64),
              verifier numeric(100,0),
              salt numeric(100,0),
              session_key numeric(100,0)
            )
          """.execute().apply()

          Account.create("T", new Srp6Protocol().computeSaltAndVerifier("T", "T"))(session)
        }
      }
    }
  }

}

object AuthServer {
  def props: Props = Props(classOf[AuthServer])

  val PreferredName = "AuthServer"
  val ActorPath = s"${Application.actorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
  val LogonProofHandlerPath = s"$ActorPath/${LogonProofHandler.PreferredName}"
  val ReconnectChallengeHandlerPath = s"$ActorPath/${ReconnectChallengeHandler.PreferredName}"
  val ReconnectProofHandlerPath = s"$ActorPath/${ReconnectProofHandler.PreferredName}"
}

