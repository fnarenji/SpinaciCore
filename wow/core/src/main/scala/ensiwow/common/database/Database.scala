package ensiwow.common.database

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

/**
  * Databases connection tokens
  */
object Databases extends Enumeration {
  /**
    * Auth database connection token
    */
  val AuthServer = Value

  /**
    * Creates a token for a realm server
    * @param id realm server id
    */
  def addRealmServer(id: Int): Unit = {
    require(id > 0)
    Value(id)
  }

  /**
    * Get database connection token for realm server
    * @param id realm server id
    * @return database connection token
    */
  def RealmServer(id: Int): Databases.Value = {
    // This assert exists because of pain and suffering
    require(id > 0)
    this (id)
  }
}
