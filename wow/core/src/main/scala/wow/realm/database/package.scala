package wow.realm

import scalikejdbc.NamedDB
import wow.common.database.Databases

/**
  * Alias methods related to database
  */
package object database {
  /**
    * Realm database connection token
    *
    * @param realm realm context
    */
  def RealmDB(implicit realm: RealmContextData): NamedDB = NamedDB(Databases.RealmServer(realm.id))
}
