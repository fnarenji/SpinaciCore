package wow.common

import scalikejdbc._
import wow.realm.{RealmContext, RealmContextData}

import scala.language.dynamics

package object database {
  case class DatabaseConfiguration(connection: String, username: String, password: String)

  /**
    * Provides an enriched column selector for classes with SQLSyntaxSupport
    * @tparam A sql syntax target type
    */
  trait RichColumn[A] extends SQLSyntaxSupport[A] {
    /**
      * Column selector
      */
    lazy val c: ColumnSelector[A] = new ColumnSelector[A]()
  }

  val AuthDB = NamedDB(Databases.AuthServer)

  def RealmDB(implicit realm: RealmContextData): NamedDB = NamedDB(Databases.RealmServer(realm.id))
}
