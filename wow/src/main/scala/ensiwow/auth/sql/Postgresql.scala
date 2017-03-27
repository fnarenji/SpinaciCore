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

      if(!tableExists("users")){
        createUsers();
      }
    }

    def execute(req: String) = {
      sql"$req".execute.apply()
    }

  def addUser(req: String) = {
    sql"insert into tesst (name) VALUES ($req)".update.apply()
  }

  def createUsers() = {
    //sql"DROP TABLE users".execute.apply()
    sql"create table users (login varchar(64), verifier numeric(77,0), salt numeric(77,0), sessionkey numeric(97,0) )".execute.apply()
  }

  def print(tableName : String) = {

    val entities = sql"select * from $tableName".map(_.toMap).list.apply();
    for (name <- entities) println(name)

  }

  def tableExists(nomTable : String) : Boolean = {
    val entities = sql"SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name=$nomTable)".map(_.toMap).list.apply();
    return (entities.head getOrElse ("exists",false)) == true
  }


}
