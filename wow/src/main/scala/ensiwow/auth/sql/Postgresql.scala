package ensiwow.auth.sql

import scalikejdbc.{AutoSession, ConnectionPool}
import scalikejdbc._

/**
  * Created by betcheg on 19/02/17.
  */
class Postgresql {

  implicit val session = AutoSession

    def init() = {
      // initialize JDBC driver & connection pool
      Class.forName("org.postgresql.Driver")
      ConnectionPool.singleton("jdbc:postgresql:bdd", "postgres", "aaaaaa64")

    }

    def execute(req: String) = {
      sql"$req".execute.apply()
    }


}
