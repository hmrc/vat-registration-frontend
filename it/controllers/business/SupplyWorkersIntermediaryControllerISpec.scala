
package controllers.business

import common.enums.VatRegStatus
import featureswitch.core.config.{OtherBusinessInvolvement, TaskList}
import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Individual, PartyType, UkCompany, VatScheme}
import models.{Business, LabourCompliance}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SupplyWorkersIntermediaryControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "intermediary workers controller" should {
    disable(OtherBusinessInvolvement)
    "return OK on show and users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Business].contains(fullModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/arrange-supply-of-workers").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return SEE_OTHER on submit redirecting to party type resolver for UkCompany" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      enable(TaskList)
      verifyRedirectForGivenPartyType(_, controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyRedirectForGivenPartyType(UkCompany, controllers.routes.TradingNameResolverController.resolve.url)
    }

    "return SEE_OTHER on submit redirecting to party  type resolver for sole trader" in new Setup {
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      enable(TaskList)
      verifyRedirectForGivenPartyType(_, controllers.routes.TaskListController.show.url)
      disable(TaskList)
      verifyRedirectForGivenPartyType(Individual, controllers.routes.TradingNameResolverController.resolve.url)
    }
  }

  private def verifyRedirectForGivenPartyType(partyType: PartyType, redirectUrl: String) = {
    val initialModel = fullModel.copy(labourCompliance = None)
    val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(None, Some(true), None)))
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(initialModel)
      .vatScheme.contains(
      VatScheme(id = currentProfile.registrationId,
        status = VatRegStatus.draft,
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = partyType))
      )
    )
      .vatScheme.isUpdatedWith[Business](expectedModel)
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .registrationApi.replaceSection[Business](expectedModel)
      .s4lContainer[Business].clearedByKey

    val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
    }
  }
}
