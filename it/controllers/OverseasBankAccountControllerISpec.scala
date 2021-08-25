

package controllers

import itutil.ControllerISpec
import models.api.NETP
import models.{BankAccount, OverseasBankDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class OverseasBankAccountControllerISpec extends ControllerISpec {

  val url: String = controllers.routes.OverseasBankAccountController.showOverseasBankAccountView().url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "return an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = true, None, Some(OverseasBankDetails("testName", "123456", "12345678")), None))
        .vatScheme.contains(vatReg.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP))))

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
        .user.isAuthorised
        .audit.writesAudit()
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
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show().url)
      }
    }
  }

}
