package wow.common.database

import scalikejdbc.interpolation.SQLSyntax

import scala.language.dynamics
import scala.language.experimental.macros

/**
  * Implements dynamic selection of columns based on analysis of class contents by macro
  *
  * @tparam A sql syntax target type
  */
final class ColumnSelector[A] extends Dynamic {
  def selectDynamic(name: String): SQLSyntax = macro wow.common.database.ExtraFieldsSQLInterpolationMacro.selectDynamic[A]
}

