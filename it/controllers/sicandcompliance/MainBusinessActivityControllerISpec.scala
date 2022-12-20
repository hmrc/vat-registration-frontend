

package controllers.sicandcompliance

import helpers.RequestsFinder
import itutil.ControllerISpec
import models.Business
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.test.Helpers._

class MainBusinessActivityControllerISpec extends ControllerISpec with RequestsFinder {

  "MainBusinessActivity on show returns OK" in new Setup {
    given()
      .user.isAuthorised()

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(routes.MainBusinessActivityController.show.url).get()
    whenReady(response) { res =>
      res.status mustBe OK
    }
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete" in new Setup {
    val incompleteModelWithoutSicCode = fullModel.copy(mainBusinessActivity = None)
    val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
    given()
      .user.isAuthorised()
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
      .registrationApi.getSection[Business](Some(expectedUpdateToBusiness))

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(routes.MainBusinessActivityController.submit.url).post(Map("value" -> Seq(sicCodeId)))
    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve.url)
    }
  }

}
