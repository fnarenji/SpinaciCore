package wow.auth

import akka.actor.{Actor, ActorLogging, Props}
import scalikejdbc._
import scodec.bits.BitVector
import wow.Application
import wow.auth.AuthServer.RegisterRealm
import wow.auth.crypto.Srp6Protocol
import wow.auth.data.Account
import wow.auth.protocol.packets.{ServerRealmlist, ServerRealmlistEntry}
import wow.auth.session.AuthSession
import wow.auth.utils.PacketSerializer
import wow.common.VersionInfo
import wow.common.database.{DatabaseConfiguration, Databases}
import wow.common.database._
import wow.common.network.TCPServer

import scala.collection.mutable


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

  context.actorOf(TCPServer.props(AuthSession, config.host, config.port), TCPServer.PreferredName)

  private val realms = mutable.HashMap[Int, ServerRealmlistEntry]()

  override def receive: Receive = {
    case RegisterRealm(id, name, host, port) =>
      log.debug(s"Realm added to list: $name at $host:$port")
      realms(id) = ServerRealmlistEntry(1, 0, 0, name, s"$host:$port", 0, 1, 1, id)
      AuthServer.realmlistPacketBits =
        PacketSerializer.serialize(ServerRealmlist(realms.values.toStream))
  }

  override def postStop(): Unit = {
    ConnectionPool.close(AuthDB)

    super.postStop()
  }

  /**
    * Creates connection to database and sets it up if necessary
    */
  private def initializeDatabase(): Unit = {
    val dbConfig = config.database

    ConnectionPool.add(Databases.AuthServer, dbConfig.connection, dbConfig.username, dbConfig.password)

    createSchema()

    def createSchema() = {
      val createDatabase = AuthDB readOnly { implicit session =>
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
              login varchar(64) unique,
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
  var realmlistPacketBits: BitVector = _

  case class RegisterRealm(id: Int, name: String, host: String, port: Int)

}

case class AuthServerConfiguration(host: String, port: Int, database: DatabaseConfiguration)

