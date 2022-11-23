
package controllers.transactor

import forms.OrganisationNameForm.organisationNameKey
import itutil.ControllerISpec
import models.TransactorDetails
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class OrganisationNameControllerISpec extends ControllerISpec {
  val url: String = controllers.transactor.routes.OrganisationNameController.show.url

  val orgName = "testOrgName"
  val testDetails = TransactorDetails(
    organisationName = Some(orgName)
  )

  s"GET $url" must {
    "show the view" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "show the view with organisation name" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .registrationApi.getSection[TransactorDetails](Some(testDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById(organisationNameKey).attr("value") mustBe orgName
      }
    }
  }

  s"POST $url" must {
    "Redirect to Declaration Capacity" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[TransactorDetails].isEmpty
        .s4lContainer[TransactorDetails].clearedByKey
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](testDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).post(Map(organisationNameKey -> orgName))

      whenReady(res) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DeclarationCapacityController.show.url)
      }
    }

    "return BAD_REQUEST for missing trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map(organisationNameKey -> "a" * 161))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
