
package controllers.registration.returns

import itutil.ControllerISpec
import models.api.{Threshold, UkCompany}
import models.api.returns.Returns
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class ClaimRefundsControllerISpec extends ControllerISpec {

  "GET /claim-vat-refunds" must {
    "Return OK when there is no value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(testApplicantIncorpDate)))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "Return OK when there is a value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, Some(true), None, None, Some(testApplicantIncorpDate)))

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
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(testApplicantIncorpDate)))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))
        .vatScheme.contains(vatReg.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.voluntaryStartPage().url)
      }
    }
    "redirect to the mandatory start date page when the user is mandatory" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, Some(testApplicantIncorpDate)))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = true)))
        .vatScheme.contains(vatReg.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.mandatoryStartPage().url)
      }
    }
  }

}
