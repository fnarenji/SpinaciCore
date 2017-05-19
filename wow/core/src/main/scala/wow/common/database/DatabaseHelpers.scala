package wow.common.database

import org.flywaydb.core.Flyway
import scalikejdbc.ConnectionPool

/**
  * Database setup helpers (e.g. migration, connection)
  */
object DatabaseHelpers {
  def connect(db: Databases.Value, dbConfig: DatabaseConfiguration): Unit = {
    ConnectionPool.add(db, dbConfig.connection, dbConfig.username, dbConfig.password)
  }

  def migrate(migrationName: String, dbConfig: DatabaseConfiguration): Unit = {
    val migration = new Flyway()

    migration.setDataSource(dbConfig.connection, dbConfig.username, dbConfig.password)
    migration.setLocations(s"classpath:db/$migrationName")
    migration.baseline()
    migration.migrate()
    migration.validate()
  }
}
