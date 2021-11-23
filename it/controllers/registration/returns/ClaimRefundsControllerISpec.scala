
package controllers.registration.returns

import featureswitch.core.config.NorthernIrelandProtocol
import itutil.ControllerISpec
import models.api.returns.Returns
import models.api._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class ClaimRefundsControllerISpec extends ControllerISpec {

  "GET /claim-vat-refunds" must {
    "Return OK when there is no value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "Return OK when there is a value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, Some(true), None, None, testApplicantIncorpDate))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /claim-vat-refunds" must {
    "redirect to the voluntary start date page when the user is voluntary" in {
      disable(NorthernIrelandProtocol)
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.voluntaryStartPage.url)
      }
    }

    "redirect to the mandatory start date page when the user is mandatory" in {
      disable(NorthernIrelandProtocol)
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.returns.routes.ReturnsController.mandatoryStartPage.url)
      }
    }

    "redirect to the Northern Ireland Protocol page when the user is non-NETP" in {
      enable(NorthernIrelandProtocol)
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }

    "redirect to send goods overseas page when the user is NETP" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
      }
    }

    "redirect to send goods overseas page when the user is Non UK Company" in {
      given()
        .user.isAuthorised
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[Returns].isUpdatedWith(Returns(None, Some(true), None, None, None))
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
      }
    }
  }
}
