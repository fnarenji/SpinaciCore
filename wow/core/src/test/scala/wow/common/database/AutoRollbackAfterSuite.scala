package wow.common.database

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import scalikejdbc.{DBSession, NamedDB}

/**
  * Marks a test suite as using the database and makes it so that everything is run inside a transaction that is rolled
  * back afterwards
  */
trait AutoRollbackAfterSuite extends BeforeAndAfterAll {
  self: FlatSpec =>
  type FixtureParam = DBSession

  def databaseName(): Any = Databases.UnitTests

  var database: NamedDB = _

  implicit var session: DBSession = _

  def setup(): Unit = ()

  override protected def beforeAll(): Unit = {
    database = NamedDB(databaseName())

    database.begin()
    session = database.withinTxSession()

    setup()
  }

  override protected def afterAll(): Unit = {
    database.rollbackIfActive()
    database.close()
  }
}
