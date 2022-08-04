
package controllers.transactor

import forms.DeclarationCapacityForm.{accountant, declarationCapacity, otherRole}
import itutil.ControllerISpec
import models.{DeclarationCapacityAnswer, Other, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class DeclarationCapacityControllerISpec extends ControllerISpec {

  val url: String = controllers.transactor.routes.DeclarationCapacityController.show.url

  val testOtherRole = "testOtherRole"
  val testDetails = TransactorDetails(
    declarationCapacity = Some(DeclarationCapacityAnswer(Other, Some("testOtherRole")))
  )

  s"GET $url" should {
    "show the view" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.getRegistration(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "show the view with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].contains(testDetails)
        .registrationApi.getRegistration(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Other"
        Jsoup.parse(res.body).getElementById(otherRole).attr("value") mustBe testOtherRole
      }
    }
  }

  s"POST $url" when {
    "the TransactorDetails model is incomplete" should {
      "update S4L and redirect to Transactor Identification" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .registrationApi.getRegistration(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(declarationCapacity -> accountant)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(routes.TransactorIdentificationController.startJourney.url)
      }

      "return BAD_REQUEST if role selected as other and not set" in new Setup {
        given()
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](None)
          .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
          .registrationApi.getRegistration(emptyUkCompanyVatScheme)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(""))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}