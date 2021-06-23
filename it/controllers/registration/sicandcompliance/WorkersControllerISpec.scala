
package controllers.registration.sicandcompliance

import common.enums.VatRegStatus
import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.api.{Individual, UkCompany, VatScheme}
import models.{SicAndCompliance, Workers}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class WorkersControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "workers controller" should {
    "redirect to business trading name page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.contains(
          VatScheme(id = currentProfile.registrationId,
            status = VatRegStatus.draft,
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))
          )
        )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(OK))))
        .s4lContainer[SicAndCompliance].clearedByKey
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.TradingNameController.show.url)
      }
    }

    "redirect to sole trader name page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(OK))))
        .s4lContainer[SicAndCompliance].clearedByKey
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.applicant.routes.SoleTraderNameController.show.url)
      }
    }
  }

}
