

package controllers.sicandcompliance

import featureswitch.core.config.TaskList
import helpers.RequestsFinder
import itutil.ControllerISpec
import models.{Business, ModelKeys}
import models.api.{EligibilitySubmissionData, NonUkNonEstablished}
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.test.Helpers._

class MainBusinessActivityControllerISpec extends ControllerISpec with RequestsFinder {

  "MainBusinessActivity on show returns OK" in new Setup {
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(routes.MainBusinessActivityController.show.url).get()
    whenReady(response) { res =>
      res.status mustBe OK
    }
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete for UkCompany" in new Setup {

    private def verifyRedirect(redirectUrl: String) = {
      val incompleteModelWithoutSicCode = fullModel.copy(mainBusinessActivity = None)
      val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(incompleteModelWithoutSicCode)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
        .s4lContainer[Business].clearedByKey

      insertIntoDb(sessionId, sicCodeMapping)
      val response = buildClient(routes.MainBusinessActivityController.submit.url).post(Map("value" -> Seq(sicCodeId)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
      }
    }

    enable(TaskList)
    verifyRedirect(controllers.routes.TaskListController.show.url)
    disable(TaskList)
    verifyRedirect(controllers.vatapplication.routes.ImportsOrExportsController.show.url)
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete for NonUkCompany" in new Setup {

    private def verifyRedirect(redirectUrl: String) = {
      val incompleteModelWithoutSicCode: Business = fullModel.copy(mainBusinessActivity = None)
      val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(incompleteModelWithoutSicCode)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
        .s4lContainer[Business].clearedByKey

      insertIntoDb(sessionId, sicCodeMapping)

      val response = buildClient(routes.MainBusinessActivityController.submit.url).post(Map("value" -> Seq(sicCodeId)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
      }
    }

    enable(TaskList)
    verifyRedirect(controllers.routes.TaskListController.show.url)
    disable(TaskList)
    verifyRedirect(controllers.vatapplication.routes.TurnoverEstimateController.show.url)
  }

}
