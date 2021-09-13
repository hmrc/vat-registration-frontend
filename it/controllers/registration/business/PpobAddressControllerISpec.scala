
package controllers.registration.business

import itutil.ControllerISpec
import models.BusinessContact
import play.api.http.HeaderNames
import play.api.test.Helpers._

class PpobAddressControllerISpec extends ControllerISpec {

  "GET /principal-place-business" should {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised
        .alfeJourney.initialisedSuccessfully()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.registration.business.routes.PpobAddressController.startJourney().url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
      }
    }
    "return INTERNAL_SERVER_ERROR when not authorised" in new Setup {
      given()
        .user.isNotAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient(controllers.registration.business.routes.PpobAddressController.startJourney().url).get()
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR

      }
    }
  }

  "GET /principal-place-business/acceptFromTxm" should {
    "return SEE_OTHER save to vat as model is complete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.registration.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.BusinessContactDetailsController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/business-contact")
        json mustBe validBusinessContactDetailsJson
      }

    }
    "returnFromTxm should return SEE_OTHER save to s4l as model is incomplete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")
        .s4lContainer[BusinessContact].isUpdatedWith(validBusinessContactDetails.copy(companyContactDetails = None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.registration.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.business.routes.BusinessContactDetailsController.show().url)
      }
    }
  }

}
