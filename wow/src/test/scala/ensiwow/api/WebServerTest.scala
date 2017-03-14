package ensiwow.api

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat
import ensiwow.auth.data.Account

/**
  * Testing WebServer's responses to account's oriented requests.
  */
class WebServerTest extends WordSpec with Matchers with ScalatestRouteTest {
  implicit val userFormat: RootJsonFormat[Account] = jsonFormat2(Account.apply)

  "The service" should {
    "return a creation success code when an account is create" in {
      Post("/account/create", Account("myName", "myPass")) ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.Created
      }
    }
    "return a success code when an account is deleted" in {
      Post("/account/delete", "myName") ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
    "return a success code when a password is reinitialized" in {
      Put("/account/reinitialize", Account("myName", "myPass")) ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
