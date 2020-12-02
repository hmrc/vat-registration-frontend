
package controllers.registration.sicandcompliance

import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
import models.{SicAndCompliance, Workers}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class WorkersControllerISpec extends ControllerISpec with SicAndComplianceFixture {

  "workers controller" should {
    "redirect to trading name page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(workers = Some(Workers(OK))))
        .s4lContainer[SicAndCompliance].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/how-many-workers-does-company-provide-at-one-time").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.TradingNameController.show.url)
      }
    }
  }

}
