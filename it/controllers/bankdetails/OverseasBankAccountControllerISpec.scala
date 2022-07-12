

package controllers.bankdetails

import itutil.ControllerISpec
import models.api.NETP
import models.{BankAccount, OverseasBankDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class OverseasBankAccountControllerISpec extends ControllerISpec {

  val url: String = routes.OverseasBankAccountController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "return an OK with pre-populated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = true, None, Some(OverseasBankDetails("testName", "123456", "12345678")), None))
        .vatScheme.contains(fullVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200

        Jsoup.parse(res.body).getElementById("name").`val`() mustBe "testName"
        Jsoup.parse(res.body).getElementById("bic").`val`() mustBe "123456"
        Jsoup.parse(res.body).getElementById("iban").`val`() mustBe "12345678"
      }
    }
  }

  s"POST $url" must {
    "return a redirect to flatrate scheme" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, None))
        .vatScheme.isUpdatedWith[BankAccount](BankAccount(isProvided = true, None, Some(OverseasBankDetails("testName", "123456", "12345678")), None))
        .s4lContainer[BankAccount].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map(
        "name" -> "testName",
        "bic" -> "123456",
        "iban" -> "12345678"
      ))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }

    "return BAD_REQUEST if any of the mandatory form fields are missing" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map(
        "bic" -> "123456",
        "iban" -> "12345678"
      ))

      whenReady(response) { res =>
        res.status mustBe 400
      }
    }

    "return BAD_REQUEST if any of the form fields are invalid" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map(
        "name" -> "testName",
        "bic" -> "123456",
        "iban" -> "ABCDEF/"
      ))

      whenReady(response) { res =>
        res.status mustBe 400
      }
    }
  }
}
