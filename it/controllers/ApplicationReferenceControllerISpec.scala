
package controllers

import connectors.RegistrationApiConnector.applicationReferenceKey
import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.ApiKey
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ApplicationReferenceControllerISpec extends ControllerISpec
  with ITRegistrationFixtures
  with FeatureSwitching {

  val testAppRef = "testAppRef"
  val url = "/register-for-vat/application-reference"


  "GET /application-reference" when {
    "a reference already exists in the users' registration" must {
      "return OK with a pre-filled form" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given
          .user.isAuthorised()
          .registrationApi.getSection(Some(testAppRef))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        whenReady(buildClient(url).get()) { res =>
          res.status mustBe OK
          Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe testAppRef
        }
      }
    }
    "a reference doesn't exist in the users'registration" must {
      "return OK with a pre-filled form" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given
          .user.isAuthorised()
          .registrationApi.getSection(None)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        whenReady(buildClient(url).get()) { res =>
          res.status mustBe OK
          Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe ""
        }
      }
    }
  }

  "POST /application-reference" when {
    "submitted with valid reference value" must {
      "successfully redirect to honesty_declaration page" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given
          .user.isAuthorised()
          .registrationApi.replaceSection(testAppRef)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Map("value" -> testAppRef))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
        }
      }
    }

    "submitted with valid a missing reference value" must {
      "return a BAD_REQUEST" in new Setup {
        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post("")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }

    "submitted with an invalid reference number, too long" must {
      "return a BAD_REQUEST" in new Setup {
        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post("w" * 101)

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }

    "submitted with an invalid reference number, invalid characters" must {
      "return a BAD_REQUEST" in new Setup {
        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post("«test»")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
