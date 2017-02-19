package ensiwow.auth.sql

/**
  * Created by betcheg on 19/02/17.
  */

import scalikejdbc._

object BasicSQLTest {
  def main(args: Array[String]): Unit = {

    val bd = new Postgresql()
    bd.init()

    implicit val session = AutoSession
    sql"""DROP TABLE tesst""".execute.apply()
    sql"""create table tesst (name varchar(64))""".execute.apply()
    sql"insert into tesst (name) VALUES ('foo')".update.apply()
    sql"insert into tesst (name) VALUES ('bar')".update.apply()

    val entities: List[Map[String, Any]] = sql"select * from tesst".map(_.toMap).list.apply()
    for (name <- entities) println(name)
  }

}