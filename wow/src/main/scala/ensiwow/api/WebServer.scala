package ensiwow.api

import java.io.File
import java.util.ServiceLoader

import akka.http.scaladsl.model.HttpRequest

import scala.reflect.runtime.{universe => ru}
import reflect.runtime.currentMirror
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import ensiwow.auth.data.AccountAPI
import org.clapper.classutil.ClassFinder

import scala.language.postfixOps


/**
  * This object implements a web server which interacts with the database.
  */

trait API {
  val route: Route
  val test: Int
}

object WebServer {
  /**
    * Retrives a list of the object's instances implementing the trait API.
    */
  val route: Route = {
    val classpath: List[File] = List(".").map(new File(_))
    val classes = ClassFinder(classpath).getClasses()
    val classMap = ClassFinder.classInfoMap(classes)
    val name = classOf[API].getName
    val apiClasses = ClassFinder.concreteSubclasses(name, classMap)

    var route: Route = RouteDirectives.reject
    val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)

    for (apiClass <- apiClasses) {
      val module = runtimeMirror.staticModule(apiClass.name)
      runtimeMirror.reflectModule(module).instance match {
        case o: API => route = route ~ o.route
      }
    }
    route
  }

}

