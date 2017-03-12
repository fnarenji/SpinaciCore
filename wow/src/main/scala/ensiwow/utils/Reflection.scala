package ensiwow.utils

import java.io.File

import org.clapper.classutil.ClassFinder

import scala.reflect.ClassTag

/**
  * Reflections helpers.
  */
object Reflection {
  /**
    * Finds all objects inheriting from type T
    * @param classTag class of T
    * @tparam T type from which objects should inherit
    * @return list of inheriting objects
    */
  def objectsOf[T](implicit classTag: ClassTag[T]): List[T] = {
    val classpath: List[File] = List(".").map(new File(_))
    val classes = ClassFinder(classpath).getClasses()
    val classMap = ClassFinder.classInfoMap(classes)
    val apiClasses = ClassFinder.concreteSubclasses(classTag.runtimeClass, classMap)

    val runtimeMirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)

    apiClasses
      .map(_.name)
      .map(runtimeMirror.staticModule)
      .map(runtimeMirror.reflectModule)
      .map(_.instance)
      .map(_.asInstanceOf[T])
      .toList
  }
}
