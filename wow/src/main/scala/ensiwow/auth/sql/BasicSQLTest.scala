package ensiwow.auth.sql

/**
  * Created by betcheg on 19/02/17.
  */

import scalikejdbc._

object BasicSQLTest {
  def main(args: Array[String]): Unit = {

    val bd = new Postgresql()
    bd.init()
    bd.create("tesst")
    bd.addUser("foo")
    bd.addUser("bar")
    bd.addUser("foobar")

    bd.print("'")

    if(bd.tableExists("tesst")) println("TRUE")
    if(bd.tableExists("fakename")) println("FALSE")
  }

}