package ensiwow.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ensiwow.auth.data.Account
import ensiwow.auth.data.AccountAPI.AccountReq
import ensiwow.utils.Reflection
import org.scalatest.{Matchers, WordSpec}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

/**
  * Testing WebServer's responses to account's oriented requests.
  */
class WebServerTest extends WordSpec with Matchers with ScalatestRouteTest {
  Reflection.eagerLoadClasses()

  implicit val userFormat: RootJsonFormat[AccountReq] = jsonFormat2(AccountReq.apply)

  "The service" should {
    "return a creation success code when an account is create" in {
      Post("/account/create", AccountReq("myName", "myPass")) ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.Created
      }
    }
    "return a success code when an account is deleted" in {
      Post("/account/delete", "myName") ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
    "return a success code when a password is reinitialized" in {
      Put("/account/reinitialize", AccountReq("myName", "myPass")) ~> WebServer.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
