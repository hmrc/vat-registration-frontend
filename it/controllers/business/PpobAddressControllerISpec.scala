
package controllers.business

import itutil.ControllerISpec
import models.BusinessContact
import play.api.http.HeaderNames
import play.api.test.Helpers._

class PpobAddressControllerISpec extends ControllerISpec {

  "GET /principal-place-business" should {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.business.routes.PpobAddressController.startJourney.url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
      }
    }
    "return INTERNAL_SERVER_ERROR when not authorised" in new Setup {
      given()
        .user.isNotAuthorised

      val response = buildClient(controllers.business.routes.PpobAddressController.startJourney.url).get()
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR

      }
    }
  }

  "GET /principal-place-business/acceptFromTxm" should {
    "return SEE_OTHER save to vat as model is complete" in new Setup {
      given()
        .user.isAuthorised()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .registrationApi.replaceSection[BusinessContact](validBusinessContactDetails, testRegId)(BusinessContact.apiKey, BusinessContact.apiFormat)
        .s4lContainer[BusinessContact].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessEmailController.show.url)
      }

    }
    "returnFromTxm should return SEE_OTHER save to s4l as model is incomplete" in new Setup {
      given()
        .user.isAuthorised()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .registrationApi.getSection[BusinessContact](None, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessEmailController.show.url)
      }
    }
  }

}
