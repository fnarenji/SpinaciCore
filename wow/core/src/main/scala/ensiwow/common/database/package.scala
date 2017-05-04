package ensiwow.common

import scala.language.experimental.macros
import scala.language.dynamics
import scalikejdbc._

package object database {

  /**
    * Implements dynamic selection of columns based on analysis of class contents by macro
    * @tparam A sql syntax target type
    */
  final class ColumnSelector[A] extends Dynamic {
    def selectDynamic(name: String): SQLSyntax = macro ensiwow.common.database.ExtraFieldsSQLInterpolationMacro.selectDynamic[A]
  }

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
}
