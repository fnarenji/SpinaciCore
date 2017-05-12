package wow.common.database

/**
  * Elements of configuration necessary for a database connection
  */
case class DatabaseConfiguration(connection: String, username: String, password: String)
