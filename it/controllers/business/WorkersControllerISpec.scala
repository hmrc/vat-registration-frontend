
package controllers.business

import featureswitch.core.config.FeatureSwitching
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NonUkNonEstablished}
import models.{Business, LabourCompliance}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._
import views.html.sicandcompliance.workers

class WorkersControllerISpec extends ControllerISpec with FeatureSwitching {

  val view = app.injector.instanceOf[workers]

  "show" should {
    "return OK with the form unpopulated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("#numberOfWorkers").attr("value") mustBe ""
      }
    }
    "return OK with the form prepopulated" in new Setup {
      val dataModel = fullModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(dataModel))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("#numberOfWorkers").attr("value") mustBe "1"
      }
    }
  }

  "submit" should {
    "redirect to the Task List" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "return BAD_REQUEST" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.replaceSection[Business](expectedModel)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }

}
