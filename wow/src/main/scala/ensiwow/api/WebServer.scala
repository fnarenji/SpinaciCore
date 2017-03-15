package ensiwow.api

import akka.http.scaladsl.server.{HttpApp, Route}
import ensiwow.utils.Reflection

import scala.language.postfixOps

/**
  * This object implements a web server which interacts with the database.
  */
trait API {
  val route: Route
}

object WebServer extends HttpApp {
  /**
    * Retrieves a list of objects implementing the trait API.
    */
  val route: Route = Reflection.objectsOf[API] map (_.route) reduce (_ ~ _)
}

