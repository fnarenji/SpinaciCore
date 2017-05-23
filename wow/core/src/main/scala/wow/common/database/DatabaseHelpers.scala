package wow.common.database

import org.flywaydb.core.Flyway
import scalikejdbc.ConnectionPool

sealed trait PlaceHolders {
  def placeHolders: Map[String, String]
}

case class RealmPlaceHolders(authSchemaName: String) extends PlaceHolders {
  override def placeHolders: Map[String, String] = Map("authSchemaName" -> authSchemaName)
}

case object AuthPlaceHolders extends PlaceHolders {
  override def placeHolders: Map[String, String] = Map.empty
}

/**
  * Database setup helpers (e.g. migration, connection)
  */
object DatabaseHelpers {
  def connect(db: DatabaseNameProvider#Value, dbConfig: DatabaseConfiguration): Unit = {
    ConnectionPool.add(db, dbConfig.connection, dbConfig.username, dbConfig.password)
  }

  def migrate(migrationName: String, dbConfig: DatabaseConfiguration, placeHolders: PlaceHolders): Unit = {
    val migration = new Flyway()

    import collection.JavaConverters._

    migration.setDataSource(dbConfig.connection, dbConfig.username, dbConfig.password)
    migration.setLocations(s"classpath:db/$migrationName")
    migration.setTable(s"${migrationName}_schema_version")
    migration.setPlaceholders(placeHolders.placeHolders.asJava)
    migration.baseline()
    migration.migrate()
    migration.validate()
  }
}
