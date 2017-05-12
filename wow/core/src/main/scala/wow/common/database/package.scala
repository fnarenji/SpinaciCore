package wow.common

import scalikejdbc.NamedDB
import wow.realm.RealmContextData

package object database {
  val AuthDB = NamedDB(Databases.AuthServer)

  def RealmDB(implicit realm: RealmContextData): NamedDB = NamedDB(Databases.RealmServer(realm.id))
}
