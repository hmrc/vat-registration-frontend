
package controllers.registration.sicandcompliance

import fixtures.SicAndComplianceFixture
import itutil.ControllerISpec
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
    "return SEE_OTHER on submit" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(fullModel)
        .vatScheme.isUpdatedWith[SicAndCompliance](fullModel.copy(intermediarySupply = Some(IntermediarySupply(true))))
        .s4lContainer[SicAndCompliance].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.TradingNameController.show().url)
      }
    }
  }

}
