
package controllers.business

import itutil.ControllerISpec
import models.BusinessContact
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class HasWebsiteControllerISpec extends ControllerISpec {

  val url = "/business-has-website"

  "GET /business-has-website" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[BusinessContact].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(hasWebsite = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'No' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(hasWebsite = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /business-has-website" must {
    "redirect to business website capture page if yes is chosen" in new Setup {
      given
        .user.isAuthorised()
        .s4l.isEmpty()
        .s4l.isUpdatedWith(BusinessContact.s4lKey.key, Json.stringify(Json.toJson(BusinessContact(hasWebsite = Some(true)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)
    }

    "redirect to contact preference capture page if no is chosen" in new Setup {
      given
        .user.isAuthorised()
        .s4l.isEmpty()
        .s4l.isUpdatedWith(BusinessContact.s4lKey.key, Json.stringify(Json.toJson(BusinessContact(hasWebsite = Some(false)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    }
  }
}