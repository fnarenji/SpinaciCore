package ensiwow.common

import scala.language.experimental.macros
import scalikejdbc._

package object database {
  final class ColumnSelector[A] extends Dynamic {
    def selectDynamic(name: String): SQLSyntax = macro ensiwow.common.database.ExtraFieldsSQLInterpolationMacro.selectDynamic[A]
  }

  trait RichColumn[A] extends SQLSyntaxSupport[A] {
    def c: ColumnSelector[A] = new ColumnSelector[A]()
  }
}
