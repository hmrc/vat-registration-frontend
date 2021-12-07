
package controllers.registration.bankdetails

import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.BankAccount
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers.await
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class UKBankAccountDetailsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/account-details"

  "GET /account-details" must {
    "return OK with a blank form if the VAT scheme doesn't contain bank details" in new Setup {
      given
        .user.isAuthorised
        .vatScheme.has("honesty-declaration", Json.obj("honestyDeclaration" -> true))
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.obj()))
        .vatScheme.doesNotExistForKey("bank-account")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
    "return OK with a pre-populated form from S4L" in new Setup {
      given
        .user.isAuthorised
        .vatScheme.has("honesty-declaration", Json.obj("honestyDeclaration" -> true))
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(bankAccount)))
        .vatScheme.doesNotExistForKey("bank-account")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("input[id=accountName]").`val`() mustBe testBankName
      doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
      doc.select("input[id=sortCode]").`val`() mustBe testSortCode
    }
    "return OK with a pre-populated form from the backend" in new Setup {
      given
        .user.isAuthorised
        .vatScheme.has("honesty-declaration", Json.obj("honestyDeclaration" -> true))
        .s4l.isEmpty()
        .vatScheme.has("bank-account", Json.toJson(bankAccount))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("input[id=accountName]").`val`() mustBe testBankName
      doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
      doc.select("input[id=sortCode]").`val`() mustBe testSortCode
    }
  }

  "POST /account-details" must {
    "redirect to the Join Flat Rate page if the bank details are valid" in new Setup {
      given
        .user.isAuthorised
        .bankAccountReputation.passes
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(isProvided = true, None, None, None))))
        .s4lContainer[BankAccount].cleared
        .s4l.isUpdatedWith(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(isProvided = true, details = Some(testUkBankDetails), None, None))))
        .vatScheme.isUpdatedWith[BankAccount](BankAccount(isProvided = true, details = Some(testUkBankDetails), None, None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj(
        "accountName" -> testBankName,
        "accountNumber" -> testAccountNumber,
        "sortCode" -> "123456"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
    }
    "return BAD_REQUEST if Bank Account Reputation states the bank details are invalid" in new Setup {
      given
        .user.isAuthorised
        .bankAccountReputation.fails
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(isProvided = true, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj(
        "accountName" -> testBankName,
        "accountNumber" -> testAccountNumber,
        "sortCode" -> "123456"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if the entered bank details are not correct" in new Setup {
      given
        .user.isAuthorised
        .bankAccountReputation.fails
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(isProvided = true, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj(
        "accountName" -> "",
        "accountNumber" -> "",
        "sortCode" -> ""
      )))

      res.status mustBe BAD_REQUEST
    }
  }

}
