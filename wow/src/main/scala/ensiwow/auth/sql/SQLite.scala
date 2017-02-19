package ensiwow.auth.sql

/**
  * Created by betcheg on 19/02/17.
  */

import scala.slick.jdbc.meta.MTable
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.StaticQuery


class SQLite(name: String) {

  val database = Database.forURL(
    "jdbc:sqlite:%s.db" format name,
    driver = "org.sqlite.JDBC")


  // User + 4 autres champs
/*
  implicit class DatabaseOps(database: Database) {
    def apply(sql: String) {
      database withSession {
        StaticQuery.updateNA(sql).execute
      }
    }

    def tableNames(): Set[String] = database withSession {
      (MTable.getTables.list() map {
        _.name.name
      }).toSet
    }
  } */

  /*
  TODO: May be useful

  def getTableExists( tableName : String ) : Boolean = {
    val tableList = MTable.getTables.list()
    for(currentName <- tableList){
      if(currentName == tableName) return true
    }
    return false
  }
*/
  class T_Users(tag: Tag) extends Table[(String, String, String, String, String)](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def pseudo = column[String]("PSEUDO")
    def foo = column[String]("FOO")
    def bar = column[String]("BAR")
    def foobar = column[String]("FOOBAR")
    def * = (id, pseudo, foo, bar, foobar)

  }
  val users = TableQuery[T_Users]

}