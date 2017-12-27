package wow.common.database

import scalikejdbc._

/**
  * Database management
  */
object Database {
  def configure(): Unit = {
    GlobalSettings.loggingSQLAndTime = GlobalSettings.loggingSQLAndTime.copy(
      printUnprocessedStackTrace = false,
      stackTraceDepth = 0,
      singleLineMode = true)
  }
}

trait DatabaseNameProvider extends Enumeration {
  def UnitTests: Value
  def AuthServer: Value
  def RealmServer(realmId: Int): Value
  def registerRealm(realmId: Int): Unit
}


/**
  * Databases connection tokens
  */
object DefaultDatabases extends Enumeration with DatabaseNameProvider{
  /**
    * Auth database connection token
    */
  private val authServer = Value

  override def AuthServer: Value = authServer

  override def UnitTests: Value = throw new UnsupportedOperationException

  /**
    * Creates a token for a realm server
    * @param id realm server id
    */
  override def registerRealm(id: Int): Unit = {
    require(id > 0)
    Value(id)
  }

  /**
    * Get database connection token for realm server
    * @param id realm server id
    * @return database connection token
    */
  override def RealmServer(id: Int): Value = {
    // This assert exists because of pain and suffering
    require(id > 0)
    this (id)
  }
}

object UnitTestDatabases extends Enumeration with DatabaseNameProvider {
  private val unitTest = Value

  override def UnitTests: Value = unitTest

  override def AuthServer: Value = unitTest

  override def RealmServer(realmId: Int): Value = unitTest

  override def registerRealm(realmId: Int): Unit = ()
}

