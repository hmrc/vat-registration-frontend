
package controllers.vatapplication

import itutil.ControllerISpec
import models.api._
import models.api.vatapplication.VatApplication
import models.{NonUk, TransferOfAGoingConcern}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class ClaimRefundsControllerISpec extends ControllerISpec {

  val testLargeTurnover = 1000000
  val testSmallTurnover = 19999
  val testZeroRated = 10000
  val testLargeTurnoverApplication: VatApplication = VatApplication(turnoverEstimate = Some(testLargeTurnover), appliedForExemption = None, zeroRatedSupplies = Some(testZeroRated))
  val testSmallTurnoverApplication: VatApplication = VatApplication(turnoverEstimate = Some(testSmallTurnover), appliedForExemption = Some(true), zeroRatedSupplies = Some(testZeroRated))

  "GET /claim-vat-refunds" must {
    "Return OK when there is no value for 'claim refunds' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
    "Return OK when there is a value for 'claim refunds' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(claimVatRefunds = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

  "POST /claim-vat-refunds" when {
    "user is not eligible for exemption" must {
      "redirect to the bank account details page when the user is TOGC/COLE" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testLargeTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "redirect to the bank account details page when the user is non-NETP" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testLargeTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NETP" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testLargeTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP, registrationReason = NonUk)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NonUkNoNEstablished" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testLargeTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished, registrationReason = NonUk)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
        }
      }
    }

    "user is eligible for exemption" must {
      "redirect to Vat Exemption page when the user answers Yes" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testSmallTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testSmallTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.VatExemptionController.show.url)
        }
      }

      "follow normal logic and clear down stored exemption answer when the user answers No" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[VatApplication].contains(testSmallTurnoverApplication)
          .s4lContainer[VatApplication].isUpdatedWith(testSmallTurnoverApplication.copy(claimVatRefunds = Some(true), appliedForExemption = None))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Json.obj("value" -> "false"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
        }
      }

      "return BAD_REQUEST if no option was selected" in new Setup {
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post("")

        whenReady(res) { result =>
          result.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
