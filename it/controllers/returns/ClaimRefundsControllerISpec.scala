
package controllers.returns

import itutil.ControllerISpec
import models.api._
import models.api.returns.Returns
import models.{NonUk, TransferOfAGoingConcern}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class ClaimRefundsControllerISpec extends ControllerISpec {

  val testLargeTurnover = 1000000
  val testSmallTurnover = 19999
  val testZeroRated = 10000
  val testLargeReturns: Returns = Returns(Some(testLargeTurnover), None, Some(testZeroRated))
  val testSmallReturns: Returns = Returns(Some(testSmallTurnover), Some(true), Some(testZeroRated))

  "GET /claim-vat-refunds" must {
    "Return OK when there is no value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(Some(testTurnover), None, None, None, None, None, testApplicantIncorpDate))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "Return OK when there is a value for 'claim refunds' in the backend" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(Some(testTurnover), None, None, Some(true), None, None, testApplicantIncorpDate))

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /claim-vat-refunds" when {
    "user is not eligible for exemption" must {
      "redirect to the bank account details page when the user is TOGC/COLE" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testLargeReturns)
          .s4lContainer[Returns].isUpdatedWith(testLargeReturns.copy(reclaimVatOnMostReturns = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "redirect to the bank account details page when the user is non-NETP" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testLargeReturns)
          .s4lContainer[Returns].isUpdatedWith(testLargeReturns.copy(reclaimVatOnMostReturns = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NETP" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testLargeReturns)
          .s4lContainer[Returns].isUpdatedWith(testLargeReturns.copy(reclaimVatOnMostReturns = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP, registrationReason = NonUk)))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NonUkNoNEstablished" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testLargeReturns)
          .s4lContainer[Returns].isUpdatedWith(testLargeReturns.copy(reclaimVatOnMostReturns = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished, registrationReason = NonUk)))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
        }
      }
    }

    "user is eligible for exemption" must {
      "redirect to Vat Exemption page when the user answers Yes" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testSmallReturns)
          .s4lContainer[Returns].isUpdatedWith(testSmallReturns.copy(reclaimVatOnMostReturns = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.VatExemptionController.show.url)
        }
      }

      "follow normal logic and clear down stored exemption answer when the user answers No" in {
        given()
          .user.isAuthorised()
          .s4lContainer[Returns].contains(testSmallReturns)
          .s4lContainer[Returns].isUpdatedWith(testSmallReturns.copy(reclaimVatOnMostReturns = Some(true), appliedForExemption = None))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "false"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "return BAD_REQUEST if no option was selected" in {
        given().user.isAuthorised()

        val res = buildClient("/claim-vat-refunds").post("")

        whenReady(res) { result =>
          result.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
