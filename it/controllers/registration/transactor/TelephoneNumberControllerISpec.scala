
package controllers.registration.transactor

import forms.TransactorTelephoneForm.telephoneNumberKey
import itutil.ControllerISpec
import models.TransactorDetails
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class TelephoneNumberControllerISpec extends ControllerISpec {
  val url = controllers.registration.transactor.routes.TelephoneNumberController.show.url

  private val testPhoneNumber = "12345 123456"
  val testDetails = TransactorDetails(
    telephone = Some(testPhoneNumber)
  )

  s"GET $url" should {
    "show the view" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "show the view with organisation name" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].contains(testDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById(telephoneNumberKey).attr("value") mustBe testPhoneNumber
      }
    }
  }

  s"POST $url" should {
    "Redirect to Transactor Email Address page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map(telephoneNumberKey -> testPhoneNumber))

      whenReady(res) { res =>
        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.transactor.routes.TransactorCaptureEmailAddressController.show.url)
      }
    }
  }
}
