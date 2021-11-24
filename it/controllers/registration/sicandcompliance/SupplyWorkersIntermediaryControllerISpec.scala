
package controllers.registration.sicandcompliance

import common.enums.VatRegStatus
import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.api.{Individual, UkCompany, VatScheme}
import models.{IntermediarySupply, SicAndCompliance}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class SupplyWorkersIntermediaryControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "intermediary workers controller" should {
    "return OK on show and users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/arrange-supply-of-workers").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return SEE_OTHER on submit redirecting to party type resolver for UkCompany" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.contains(
          VatScheme(id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData)
          )
        )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(intermediarySupply = Some(IntermediarySupply(true))))
        .s4lContainer[SicAndCompliance].clearedByKey
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
      }
    }

    "return SEE_OTHER on submit redirecting to party  type resolver for sole trader" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(intermediarySupply = Some(IntermediarySupply(true))))
        .s4lContainer[SicAndCompliance].clearedByKey
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
      }
    }
  }
}
