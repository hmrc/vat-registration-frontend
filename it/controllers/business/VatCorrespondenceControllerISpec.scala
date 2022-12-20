
package controllers.business

import forms.VatCorrespondenceForm
import itutil.ControllerISpec
import models.Business
import models.api.vatapplication.VatApplication
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

class VatCorrespondenceControllerISpec extends ControllerISpec {

  val url: String = routes.VatCorrespondenceController.show.url

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").toArray() mustBe Nil
      }
    }
    "return OK with 'English' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(Business(welshLanguage = Some(false))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "English"
      }
    }
    "return OK with 'Welsh' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(Business(welshLanguage = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Welsh"
      }
    }
  }

  s"POST $url" must {
    "redirect to contact preference page" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(welshLanguage = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> VatCorrespondenceForm.welsh)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    }
    
    "return BAD_REQUEST if no option is selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}