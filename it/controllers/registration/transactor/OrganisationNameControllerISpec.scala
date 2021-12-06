
package controllers.registration.transactor

import forms.OrganisationNameForm.organisationNameKey
import itutil.ControllerISpec
import models.TransactorDetails
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class OrganisationNameControllerISpec extends ControllerISpec {
  val url: String = controllers.registration.transactor.routes.OrganisationNameController.show.url

  val orgName = "testOrgName"
  val testDetails = TransactorDetails(
    organisationName = Some(orgName)
  )

  s"GET $url" should {
    "show the view" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)
        .vatScheme.contains(emptyUkCompanyVatScheme)

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
        .vatScheme.contains(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById(organisationNameKey).attr("value") mustBe orgName
      }
    }
  }

  s"POST $url" should {
    "Redirect to Declaration Capacity" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)
        .s4lContainer[TransactorDetails].isUpdatedWith(testDetails)
        .vatScheme.contains(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map(organisationNameKey -> organisationNameKey))

      whenReady(res) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DeclarationCapacityController.show.url)
      }
    }
  }
}
