
package controllers.business

import itutil.ControllerISpec
import models.Business
import play.api.http.HeaderNames
import play.api.test.Helpers._

class BusinessActivityDescriptionControllerISpec extends ControllerISpec {

  "GET /what-company-does" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.business.routes.BusinessActivityDescriptionController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /what-company-does" must {
    "redirect to ICL on submit" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[Business](businessDetails.copy(businessDescription = Some("foo")))
        .registrationApi.getSection[Business](Some(businessDetails.copy(businessDescription = Some("foo"))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.business.routes.BusinessActivityDescriptionController.submit.url)
        .post(Map("description" -> Seq("foo")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)
      }
    }
  }

}
