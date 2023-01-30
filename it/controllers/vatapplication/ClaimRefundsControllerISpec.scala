
package controllers.vatapplication

import itutil.ControllerISpec
import models.api._
import models.api.vatapplication.VatApplication
import models.{NonUk, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
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

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "Return OK when there is a value for 'claim refunds' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient("/claim-vat-refunds").get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  "POST /claim-vat-refunds" when {
    "user is not eligible for exemption" must {
      "redirect to the bank account details page when the user is TOGC/COLE" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "redirect to the missing answer page when the turnover answer is missing" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(turnoverEstimate = None, claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(turnoverEstimate = None, claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/claim-vat-refunds").post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }

      "redirect to the missing answer page when the zero-rated supplies answer is missing" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(zeroRatedSupplies = None, claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(zeroRatedSupplies = None, claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/claim-vat-refunds").post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }

      "redirect to the bank account details page when the user is non-NETP" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NETP" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
          partyType = NETP,
          registrationReason = NonUk,
          fixedEstablishmentInManOrUk = false
        )))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
        }
      }

      "redirect to send goods overseas page when the user is NonUkNoNEstablished" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
          partyType = NonUkNonEstablished,
          registrationReason = NonUk,
          fixedEstablishmentInManOrUk = false
        )))
          .registrationApi.replaceSection[VatApplication](testLargeTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testLargeTurnoverApplication.copy(claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "true"))

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
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection[VatApplication](testSmallTurnoverApplication.copy(claimVatRefunds = Some(true)))
          .registrationApi.getSection[VatApplication](Some(testSmallTurnoverApplication.copy(claimVatRefunds = Some(true))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "true"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.VatExemptionController.show.url)
        }
      }

      "follow normal logic and clear down stored exemption answer when the user answers No" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection[VatApplication](testSmallTurnoverApplication.copy(claimVatRefunds = Some(false), appliedForExemption = None))
          .registrationApi.getSection[VatApplication](Some(testSmallTurnoverApplication.copy(claimVatRefunds = Some(false), appliedForExemption = None)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = buildClient("/claim-vat-refunds").post(Map("value" -> "false"))

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
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
