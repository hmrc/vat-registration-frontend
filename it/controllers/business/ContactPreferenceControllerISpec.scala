
package controllers.business

import forms.ContactPreferenceForm
import itutil.ControllerISpec
import models.{Business, Email, Letter}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

class ContactPreferenceControllerISpec extends ControllerISpec {

  val url: String = routes.ContactPreferenceController.showContactPreference.url

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").toArray() mustBe Nil
      }
    }
    "return OK with 'Email' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(businessDetails.copy(contactPreference = Some(Email)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Email"
      }
    }
    "return OK with 'Letter' pre-populated" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(businessDetails.copy(contactPreference = Some(Letter)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Letter"
      }
    }
  }

  s"POST $url" must {
    "redirect to task list page" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .s4lContainer[Business].isEmpty
        .registrationApi.replaceSection[Business](Business(contactPreference = Some(Letter)))
        .s4lContainer[Business].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> ContactPreferenceForm.letter)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}