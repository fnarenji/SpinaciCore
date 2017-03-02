package ensiwow.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.language.postfixOps

/**
  * This object implements a web server which interacts with the database.
  */
object WebServer {
  implicit val system = ActorSystem("webServer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  val log = system.log

  // TODO: This should be defined in the class creating the accounts
  type Username = String
  final case class User(username: Username, password: String)

  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User)

  val routeAccounts: Route =
    post {
      pathPrefix("user") {
        path("create") {
          pathEnd {
            entity(as[User]) { user =>
              // TODO: Create account
              log.debug(s"${user.username}: Added to the database")
              complete(StatusCodes.Created)
            }
          }
        } ~
        path("reinitialize") {
          pathEnd {
            entity(as[User]) { user =>
              // TODO: Reinitialize account's password
              log.debug(s"Reinitialising ${user.username} password")
              complete(StatusCodes.OK)
            }
          }
        } ~
        path("delete") {
          pathEnd {
            entity(as[Username]) { username =>
              // TODO: Delete account
              log.debug(s"Suppressing $username account")
              complete(StatusCodes.OK)
            }
          }
        }
      }
    }

  def main(args: Array[String]) {

    val bindingFuture = Http().bindAndHandle(routeAccounts, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
