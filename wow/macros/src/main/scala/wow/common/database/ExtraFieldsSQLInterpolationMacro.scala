package wow.common.database

import scalikejdbc.interpolation.SQLSyntax

import scala.annotation.StaticAnnotation
import scala.reflect.macros._

//noinspection SpellCheckingInspection
case class databasefields(s: String*) extends StaticAnnotation

/**
  * Macros for dynamic fields validation
  */
object ExtraFieldsSQLInterpolationMacro {
  def selectDynamic[E: c.WeakTypeTag](c: blackbox.Context)(name: c.Expr[String]): c.Expr[SQLSyntax] = {
    import c.universe._

    def getName(s: c.universe.Symbol) = s.name.encodedName.toString.trim

    val tpe = c.weakTypeOf[E]
    val ctor = tpe.decl(c.universe.termNames.CONSTRUCTOR).asMethod
    // primary constructor args of type E
    val expectedNames = ctor.paramLists.flatMap { symbols: List[Symbol] => symbols.map(getName) }.toBuffer

    def parseAnnotations(symbol: Symbol) = {
      for (annotation <- symbol.annotations if annotation.tree.tpe == typeOf[databasefields]) yield {
        assert(expectedNames contains getName(symbol), s"${getName(symbol)} not present in $expectedNames")

        expectedNames -= getName(symbol)
        expectedNames ++= annotation.tree.children.tail.collect { case Literal(Constant(field: String)) => field }
      }
    }

    ctor.paramLists.flatten.foreach(parseAnnotations)

    name.tree match {
      case Literal(Constant(value: String)) =>
        if (!expectedNames.contains(value)) {
          c.error(c.enclosingPosition,
            s"${c.weakTypeOf[E]}#$value not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
        }
      case _ => None
    }

    c.Expr[SQLSyntax](q"super[SQLSyntaxSupport].column.field($name)")
  }
}

