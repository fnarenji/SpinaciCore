package wow.common.database

import scalikejdbc.interpolation.SQLSyntax

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.reflect.macros._

//noinspection SpellCheckingInspection
case class databasefields(s: String*) extends StaticAnnotation

//noinspection SpellCheckingInspection
class databasecomponent extends StaticAnnotation

//noinspection SpellCheckingInspection
class databaseprefix extends StaticAnnotation

/**
  * Macros for dynamic fields validation
  */
object ExtraFieldsSQLInterpolationMacro {
  def selectDynamic[E: c.WeakTypeTag](c: blackbox.Context)(name: c.Expr[String]): c.Expr[SQLSyntax] = {
    import c.universe._

    /**
      * Extracts name of symbol
      * @param s symbol
      * @return name
      */
    def getName(s: c.universe.Symbol) = s.name.encodedName.toString.trim

    /**
      * Is symbol annotated with databasecomponent
      * @param symbol symbol
      * @return true if annotated, false otherwise
      */
    def isComponentAnnotated(symbol: c.universe.Symbol) = symbol.annotations.exists(a => a.tree.tpe == typeOf[databasecomponent])

    def getPrefix(symbol: c.universe.Symbol) = {
      symbol.annotations.find(a => a.tree.tpe == typeOf[databaseprefix]) match {
        case Some(a) =>
          a.tree.children.tail.collectFirst({ case Literal(Constant(field: String)) => field }).get
        case None =>
          ""
      }
    }

    /**
      * Gets the primary constructor of class
      * @param tpe type for which to find primary constructor
      * @return optional primary ctor symbol
      */
    def getPrimary(tpe: c.Type) = tpe.decl(c.universe.termNames.CONSTRUCTOR).asTerm.alternatives.collectFirst {
      case ctor: MethodSymbol if ctor.isPrimaryConstructor => ctor
    }

    val tpe = c.weakTypeOf[E]
    val ctor = getPrimary(tpe).get

    val expectedNames = mutable.Buffer[String]()

    // Queue of symbols that are still to be processed
    val symbols = mutable.Queue[c.universe.Symbol](ctor)

    // While there are symbols to be explored
    while (symbols.nonEmpty) {
      val symbol = symbols.dequeue()

      // First check for databasefields annotation, as it overrides everything
      val maybeFieldsAnnotation = symbol.annotations.find(a => a.tree.tpe == typeOf[databasefields])

      if (maybeFieldsAnnotation.nonEmpty) {
        // Get the string vararg parameter of the annotation
        expectedNames ++=
          maybeFieldsAnnotation.get.tree.children.tail.collect { case Literal(Constant(field: String)) => field }
      } else if (symbol.isMethod) {
        // If the symbol is a method, get its parameters and put them in the queue
        val m = symbol.asMethod
        val paramSymbols = m.paramLists.flatten
        symbols.enqueue(paramSymbols: _*)
      } else if (symbol.isParameter) {
        // If its a parameter, find its type's primary constructor, then check if either the primary constructor or
        // the parameter is annotated with databasecomponent. If it succeeds, add it as a symbol to be parsed.
        // If not, add it as a valid column.
        getPrimary(symbol.typeSignature) match {
          case Some(m) if isComponentAnnotated(symbol.typeSignature.typeSymbol) || isComponentAnnotated(symbol) =>
            symbols += m
          case _ =>
            expectedNames += getPrefix(symbol) + getName(symbol)
        }
      } else {
        c.error(c.enclosingPosition, s"Unsupported case $symbol")
      }
    }

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

