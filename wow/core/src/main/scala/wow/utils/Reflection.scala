package wow.utils

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Reflections helpers.
  */
object Reflection {

  import scala.reflect.runtime.universe._

  private def classLoader = Thread.currentThread().getContextClassLoader

  /**
    * Scala reflection mirror
    */
  private def mirror = runtimeMirror(classLoader)

  /**
    * Map of ClassInfo by class name
    */
  private lazy val classMap: mutable.Map[String, ClassInfo] = {
    val classPathScan = new FastClasspathScanner().scan()

    classPathScan.getClassNameToClassInfo.asScala
  }

  /**
    * Should trigger eager class loading by JVM...
    *
    * Note: apparently does something, kind of know why. Is hack.
    */
  def eagerLoadClasses(): Unit = classMap.values.foreach(_.getImplementedInterfaces)

  /**
    * Finds all objects inheriting from type A
    *
    * @tparam A type from which objects should inherit
    * @return list of inheriting objects
    */
  def objectsOf[A: TypeTag]: Iterable[A] = {
    // Get generic type information
    val selfType = typeOf[A]
    val name = selfType.typeSymbol.asClass.fullName

    // Find class info about generic type and get all subclasses/classes implementing
    val classInfo = classMap(name)
    val impls = mutable.HashSet[ClassInfo]()
    impls ++= classInfo.getSubclasses.asScala
    impls ++= classInfo.getClassesImplementing.asScala

    val objects = mutable.MutableList[A]()

    for (impl <- impls) {
      // Find implementation module info
      val implName = impl.getClassName
      val moduleSymbol = mirror.staticModule(implName)

      if (moduleSymbol.isModule) {
        val module = moduleSymbol.asModule

        // Get type of module, check if subtype of generic type
        val moduleType = module.moduleClass.asClass.selfType
        if (moduleType <:< selfType) {
          val refl = mirror.reflectModule(module)
          assert(refl.isStatic)

          val inst = refl.instance
          val cast = inst.asInstanceOf[A]

          objects += cast
        }
      }
    }

    objects
  }
}
