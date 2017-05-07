package wow.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * Created by sknz on 5/3/17.
  */
object TestMacro {
  def impl(c: blackbox.Context) = {
    import c.universe._
    c.Expr[Unit](q"""println("Hello World")""")
  }

  def helloMacro: Unit = macro impl
}
