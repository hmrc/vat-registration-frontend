
package controllers.business

import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement, TaskList}
import featureswitch.core.models.FeatureSwitch
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NonUkNonEstablished}
import models.{Business, LabourCompliance}
import play.api.http.HeaderNames
import play.api.mvc.Call
import play.api.test.Helpers._

class WorkersControllerISpec extends ControllerISpec with FeatureSwitching {

  def verifyRedirectLocation(optFeatureSwitch: Option[FeatureSwitch], resolvedLocation: Call) = {
    optFeatureSwitch.foreach(enable)
    val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(resolvedLocation.url)
    }
    optFeatureSwitch.foreach(disable)
  }

  "workers controller" should {
    "have correct FS based redirects for UkCompany" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(initialModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedModel)
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      verifyRedirectLocation(Some(TaskList), controllers.routes.TaskListController.show)
      verifyRedirectLocation(None, controllers.vatapplication.routes.ImportsOrExportsController.show)
      verifyRedirectLocation(Some(OtherBusinessInvolvement), controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
    }

    "have correct FS based redirects for NonUkCompany" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(initialModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.replaceSection[Business](expectedModel)
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      verifyRedirectLocation(Some(TaskList), controllers.routes.TaskListController.show)
      verifyRedirectLocation(None, controllers.vatapplication.routes.TurnoverEstimateController.show)
      verifyRedirectLocation(Some(OtherBusinessInvolvement), controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
    }
  }

}
