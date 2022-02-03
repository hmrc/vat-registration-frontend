
package controllers.registration.sicandcompliance

import controllers.registration.sicandcompliance.{routes => sicRoutes}
import itutil.ControllerISpec
import models.{BusinessActivityDescription, SicAndCompliance}
import play.api.http.HeaderNames
import play.api.test.Helpers._

class BusinessActivityDescriptionControllerISpec extends ControllerISpec {

  "GET /what-company-does" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(sicAndCompliance)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(sicRoutes.BusinessActivityDescriptionController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /what-company-does" must {
    "redirect to ICL on submit" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[SicAndCompliance].contains(sicAndCompliance)
        .vatScheme.isUpdatedWith[SicAndCompliance](sicAndCompliance.copy(description = Some(BusinessActivityDescription("foo"))))
        .s4lContainer[SicAndCompliance].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(sicRoutes.BusinessActivityDescriptionController.submit.url)
        .post(Map("description" -> Seq("foo")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SicAndComplianceController.submitSicHalt.url)
      }
    }
  }

}
