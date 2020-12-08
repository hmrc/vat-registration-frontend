
package controllers.registration.returns

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.Threshold
import models.view.ApplicantDetails
import models.{Returns, Start}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.AppAndStubs
import play.api.test.Helpers._

class ClaimRefundsControllerISpec extends IntegrationSpecBase
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures {

  "GET /claim-vat-refunds" must {
    "Return OK when there is no value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(Start(Some(testApplicantIncorpDate)))))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "Return OK when there is a value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, Some(true), None, None, Some(Start(Some(testApplicantIncorpDate)))))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /claim-vat-refunds" must {
    "redirect to the voluntary start date page when the user is voluntary" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(Start(Some(testApplicantIncorpDate)))))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ReturnsController.voluntaryStartPage.url)
      }
    }
    "redirect to the mandatory start date page when the user is mandatory" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(Start(Some(testApplicantIncorpDate)))))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = true)))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ReturnsController.mandatoryStartPage().url)
      }
    }
  }

}
