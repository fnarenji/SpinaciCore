package wow.auth

import javax.security.auth.login.Configuration

import akka.actor.{Actor, ActorLogging, Props}
import wow.{Application, common}
import wow.auth.crypto.Srp6Protocol
import wow.auth.data.Account
import wow.auth.handlers.{LogonChallengeHandler, LogonProofHandler, ReconnectChallengeHandler, ReconnectProofHandler}
import wow.auth.protocol.packets.{ServerRealmlist, ServerRealmlistEntry}
import wow.auth.session.{AuthSession, EventRealmlist}
import wow.auth.utils.PacketSerializer
import wow.common.VersionInfo
import wow.common.database.{DatabaseConfiguration, Databases}
import wow.common.network.TCPServer
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import scalikejdbc._

case object GetRealmlist

/**
  * AuthServer is the base actor for all services provided by the authentication server.
  *
  * This actor is unique (e.g. singleton) per ActorSystem.
  * It holds ownership of the stateless packet handlers.
  */
class AuthServer extends Actor with ActorLogging {
  val config = Application.configuration.auth

  log.info(s"startup, supporting version ${VersionInfo.SupportedVersionInfo}")

  initializeDatabase()

  // TODO: should handlers be put in a pool so that they scale accordingly to the load ?
  context.actorOf(LogonChallengeHandler.props, LogonChallengeHandler.PreferredName)
  context.actorOf(LogonProofHandler.props, LogonProofHandler.PreferredName)
  context.actorOf(ReconnectChallengeHandler.props, ReconnectChallengeHandler.PreferredName)
  context.actorOf(ReconnectProofHandler.props, ReconnectProofHandler.PreferredName)
  context.actorOf(TCPServer.props(AuthSession, config.host, config.port), TCPServer.PreferredName)

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
    val dbConfig = config.database

    ConnectionPool.add(Databases.AuthServer, dbConfig.connection, dbConfig.username, dbConfig.password)

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
  val ActorPath = s"${Application.ActorPath}/$PreferredName"
  val LogonChallengeHandlerPath = s"$ActorPath/${LogonChallengeHandler.PreferredName}"
  val LogonProofHandlerPath = s"$ActorPath/${LogonProofHandler.PreferredName}"
  val ReconnectChallengeHandlerPath = s"$ActorPath/${ReconnectChallengeHandler.PreferredName}"
  val ReconnectProofHandlerPath = s"$ActorPath/${ReconnectProofHandler.PreferredName}"
}

case class AuthServerConfiguration(host: String, port: Int, database: DatabaseConfiguration)

