package ensiwow.common.database

import scalikejdbc._

/**
  * Database management
  */
object Database {
  def configure(): Unit = {
    GlobalSettings.loggingSQLAndTime = GlobalSettings.loggingSQLAndTime.copy(printUnprocessedStackTrace = false, stackTraceDepth =  0, singleLineMode = true)
  }
}

/**
  * Databases tokens
  */
object Databases extends Enumeration {
  val AuthServer = Value

  def addRealmServer(id: Int): Unit ={
    require(id > 0)
    Value(id)
  }

  def RealmServer(id: Int): Databases.Value = {
    // This assert exists because of pain and suffering
    require(id > 0)
    this (id)
  }
}
