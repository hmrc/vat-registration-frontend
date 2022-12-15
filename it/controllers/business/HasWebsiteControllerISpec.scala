
package controllers.business

import featureswitch.core.config.WelshLanguage
import itutil.ControllerISpec
import models.Business
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class HasWebsiteControllerISpec extends ControllerISpec {

  val url = "/business-has-website"

  "GET /business-has-website" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'Yes' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(businessDetails.copy(hasWebsite = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'No' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(businessDetails.copy(hasWebsite = Some(false)))

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
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(hasWebsite = Some(true)))
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)
    }

    "redirect to contact preference capture page if no is chosen" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(hasWebsite = Some(false)))
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    }

    "redirect to vat correspondence page if no is chosen and WelshLanguage feature switch is enabled" in new Setup {
      enable(WelshLanguage)
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(hasWebsite = Some(false)))
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
      disable(WelshLanguage)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}