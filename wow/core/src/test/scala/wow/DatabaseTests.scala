package wow

import org.scalatest.{BeforeAndAfterAll, Suites}
import pureconfig.loadConfigOrThrow
import scalikejdbc._
import wow.auth.data.AccountDaoTest
import wow.common.database.{AuthPlaceHolders, Database, DatabaseConfiguration, DatabaseHelpers, Databases, RealmPlaceHolders, UnitTestDatabases}
import wow.realm.objects.characters.CharacterDaoTest

/**
  * This test suite aggregates all database tests.
  * This suite initialize the test by doing a migration on the test database when the tests start.
  */
class DatabaseTests extends Suites(new AccountDaoTest, new CharacterDaoTest) with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    // Enable unit tests mode for database names
    // This makes all database names point towards the same database
    Databases = UnitTestDatabases

    Database.configure()

    val testConfiguration = loadConfigOrThrow[TestConfiguration]("wow-test")

    checkDangerousConfigurations(testConfiguration)

    try {
      // Migrate auth first, then connect to database, then use that connection's schema name to run realm migration
      DatabaseHelpers.migrate("auth", testConfiguration.test, AuthPlaceHolders)

      DatabaseHelpers.connect(Databases.UnitTests, testConfiguration.test)

      val db = NamedDB(Databases.UnitTests)
      DatabaseHelpers.migrate("realm", testConfiguration.test, RealmPlaceHolders(db.conn.getSchema))
    } catch {
      case e: Throwable =>
        cancel(s"Could not setup databases", e)
    }
  }

  /**
    * Since database tests can be potentially destructive, make sure that there is no overlap between
    * normal database connections and test database connections
    *
    * @param testConfiguration test configuration
    */
  private def checkDangerousConfigurations(testConfiguration: TestConfiguration): Unit = {
    import wow.common.config.deriveIntMap

    val applicationConfiguration = loadConfigOrThrow[ApplicationConfiguration]("wow")

    assume(applicationConfiguration.auth.database != testConfiguration.test)
    assume(applicationConfiguration.realms.values.forall(_.database != testConfiguration.test))
  }

  override protected def afterAll(): Unit = {
    ConnectionPool.closeAll()
  }
}

case class TestConfiguration(test: DatabaseConfiguration)

