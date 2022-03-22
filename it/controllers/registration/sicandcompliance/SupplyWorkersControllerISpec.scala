
package controllers.registration.sicandcompliance

import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models._
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SupplyWorkersControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "supply workers controllers" should {
    "return OK on Show AND users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/supply-of-workers").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return INTERNAL_SERVER_ERROR if not authorised on show" in new Setup {
      given()
        .user.isNotAuthorised

      val response = buildClient("/supply-of-workers").get()
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "redirect on submit to populate S4l not vat as model is incomplete" in new Setup {
      val incompleteModel = fullModel.copy(
        description = None
      )
      val toBeUpdatedModel = incompleteModel.copy(
        supplyWorkers = Some(SupplyWorkers(true)))

      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(incompleteModel)
        .s4lContainer[SicAndCompliance].isUpdatedWith(toBeUpdatedModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/supply-of-workers").post(
        Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.sicandcompliance.routes.WorkersController.show.url)
      }
    }
  }

}
