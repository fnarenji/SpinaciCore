package ensiwow.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ensiwow.utils.Reflection

import scala.language.postfixOps
import scala.reflect.runtime.{universe => ru}

/**
  * This object implements a web server which interacts with the database.
  */
trait API {
  val route: Route
}

object WebServer {
  /**
    * Retrieves a list of objects implementing the trait API.
    */
  val route: Route = Reflection.objectsOf[API] map (_.route) reduce (_ ~ _)
}

