
package controllers.business

import common.enums.VatRegStatus
import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement, TaskList}
import featureswitch.core.models.FeatureSwitch
import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Individual, VatScheme}
import models.{Business, LabourCompliance}
import play.api.http.HeaderNames
import play.api.mvc.Call
import play.api.test.Helpers._

class WorkersControllerISpec extends ControllerISpec with SicAndComplianceFixture with FeatureSwitching {

  "workers controller" should {
    "redirect to party type resolver for UkCompany" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(initialModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData)
        )
      )
        .vatScheme.isUpdatedWith[Business](expectedModel)
        .registrationApi.replaceSection[Business](expectedModel)
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      verifyRedirectOnFeatureSwitch()
    }

    "redirect to party type resolver for sole trader" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(initialModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
        .vatScheme.isUpdatedWith[Business](expectedModel)
        .registrationApi.replaceSection[Business](expectedModel)
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      verifyRedirectOnFeatureSwitch()
    }
  }

  private def verifyRedirectOnFeatureSwitch(): Unit = {

    def verifyRedirectLocation(featureSwitchFn: FeatureSwitch => Unit, resolvedLocation: Call) = {
      featureSwitchFn(OtherBusinessInvolvement)
      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(resolvedLocation.url)
      }
    }

    enable(TaskList)
    verifyRedirectLocation(disable, controllers.routes.TaskListController.show)
    disable(TaskList)
    verifyRedirectLocation(disable, controllers.routes.TradingNameResolverController.resolve(false))
    verifyRedirectLocation(enable, controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show)
  }
}
