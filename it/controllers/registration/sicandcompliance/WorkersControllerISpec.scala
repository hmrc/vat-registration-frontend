
package controllers.registration.sicandcompliance

import common.enums.VatRegStatus
import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Individual, VatScheme}
import models.{SicAndCompliance, Workers}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class WorkersControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "workers controller" should {
    "redirect to party type resolver for UkCompany" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.contains(
          VatScheme(id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData)
          )
        )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(OK))))
        .s4lContainer[SicAndCompliance].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
      }
    }

    "redirect to party type resolver for sole trader" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(OK))))
        .s4lContainer[SicAndCompliance].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
      }
    }
  }

}
