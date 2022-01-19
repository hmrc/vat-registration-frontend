
package controllers

import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import org.jsoup.Jsoup
import play.api.libs.json.Json
import support.RegistrationsApiStubs
import play.api.test.Helpers._

class ApplicationReferenceControllerISpec extends ControllerISpec
  with ITRegistrationFixtures
  with FeatureSwitching
  with RegistrationsApiStubs {

  val url = "/register-for-vat/application-reference"
  val testAppRef = "testAppRef"


  "GET /application-reference" when {
    "a reference already exists in the users' registration" must {
      "return OK with a pre-filled form" in new Setup {
        given
          .user.isAuthorised

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(emptyUkCompanyVatScheme.copy(applicationReference = Some(testAppRef)))))

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe testAppRef
      }
    }
    "a reference doesn't exist in the users'registration" must {
      "return OK with a pre-filled form" in new Setup {
        given
          .user.isAuthorised

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(emptyUkCompanyVatScheme)))

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe ""
      }
    }
  }

}
