package wow.common.database

import scalikejdbc._

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
