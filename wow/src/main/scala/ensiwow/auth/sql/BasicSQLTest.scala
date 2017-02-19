package ensiwow.auth.sql

/**
  * Created by betcheg on 19/02/17.
  */

import scala.slick.driver.H2Driver.simple._

object BasicSQLTest extends SQLite("test_users") with App {

  /*
  def addRow(id: String, value: String, foo: String, bar: String, foobar: String) =
    //database.run(sql"insert or replace into test_users values ('%s', '%s', '%s', '%s', '%s')".format(id, value, foo,bar, foobar)))
    database.run(sql"insert or replace into test_users values ('1','A','A','A','A')")


  if (!getTableExists("test_users")) {
    sqlucreate table test_users(id varchar(18), pseudo varchar(20), foo varchar(20), bar varchar(20), foobar varchar(20))"""
  } */


  database.withSession { implicit session =>
    users.ddl.create

    users += ("1", "Floran", "A", "B", "C")
    users += ("2", "Bastien", "D", "E", "F")
  }

}