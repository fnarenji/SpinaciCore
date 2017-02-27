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
      //implicit val session = AutoSession
    }

    def execute(req: String) = {
      sql"$req".execute.apply()
    }

  def addUser(req: String) = {
    sql"insert into tesst (name) VALUES ($req)".update.apply()
  }

  def create(tableName: String) = {

    sql"DROP TABLE tesst".execute.apply()
    sql"create table tesst (name varchar(64))".execute.apply()
  }


  def print(tableName : String) = {

    val entities: List[Map[String, Any]] = sql"select * from tesst".map(_.toMap).list.apply()
    for (name <- entities) println(name)

  }

  def tableExists(nomTable : String) : Boolean = {
    val entities = sql"SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name=$nomTable)".map(_.toMap).list.apply();
    return (entities.head getOrElse ("exists",false)) == true
  }


}
