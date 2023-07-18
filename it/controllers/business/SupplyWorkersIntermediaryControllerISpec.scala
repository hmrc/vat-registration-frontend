
package controllers.business

import itutil.ControllerISpec
import models.api._
import models.{Business, LabourCompliance}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SupplyWorkersIntermediaryControllerISpec extends ControllerISpec {

  "intermediary workers controller" should {
    "return OK on show and users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/arrange-supply-of-workers").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return SEE_OTHER on submit redirecting to Import of Export for UkCompany" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = None)
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(None, Some(true), None)))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))


      insertCurrentProfileIntoDb(currentProfile, sessionString)

      verifyRedirectForGivenPartyType(controllers.routes.TaskListController.show.url)
    }

    "return SEE_OTHER on submit redirecting to Turnover for NonUkCompany" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = None)
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(None, Some(true), None)))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      verifyRedirectForGivenPartyType(controllers.routes.TaskListController.show.url)
    }
  }

  private def verifyRedirectForGivenPartyType(redirectUrl: String) = {
    val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
    }
  }
}
