
package controllers.registration.bankdetails

import itutil.ControllerISpec
import models.BankAccount
import models.api.{EligibilitySubmissionData, NETP}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class HasBankAccountControllerISpec extends ControllerISpec {

  val url = "/companys-bank-account"

  "GET /companys-bank-account" must {
    "return OK with a blank form if the vat scheme doesn't contain bank details" in new Setup {
      given
        .user.isAuthorised
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.obj()))
        .vatScheme.doesNotExistForKey("bank-account")
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
    "return OK with 'Yes' pre-populated from S4L" in new Setup {
      given
        .user.isAuthorised
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(true, None, None, None))))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'Yes' pre-populated from the backend" in new Setup {
      given
        .user.isAuthorised
        .s4l.isEmpty()
        .vatScheme.has("bank-account", Json.toJson(BankAccount(true, None, None, None)))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from S4L" in new Setup {
      given
        .user.isAuthorised
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(false, None, None, None))))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from the backend" in new Setup {
      given
        .user.isAuthorised
        .s4l.isEmpty()
        .vatScheme.has("bank-account", Json.toJson(BankAccount(false, None, None, None)))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
  }

  "POST /companys-bank-account" must {
    "redirect to the UK bank page if the user has a bank account" in new Setup {
      given
        .user.isAuthorised
        .s4l.isEmpty()
        .s4l.isUpdatedWith(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(true, None, None, None))))
        .vatScheme.doesNotExistForKey("bank-account")
        .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData)))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.bankdetails.routes.UkBankAccountDetailsController.show.url)
    }
    "redirect to the Overseas bank page if the user is a NETP" in new Setup {
      given
        .user.isAuthorised
        .s4l.isEmpty()
        .s4l.isUpdatedWith(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(true, None, None, None))))
        .vatScheme.has("honesty-declaration", Json.obj("honestyDeclaration" -> true))
        .vatScheme.doesNotExistForKey("bank-account")
        .vatScheme.contains(emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = NETP))
        ))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.bankdetails.routes.OverseasBankAccountController.show.url)
    }
    "redirect to the reason for no bank account page if the user doesn't have a bank account" in new Setup {
      given
        .user.isAuthorised
        .vatScheme.has("honesty-declaration", Json.obj("honestyDeclaration" -> true))
        .s4l.isEmpty()
        .s4l.isUpdatedWith(BankAccount.s4lKey.key, Json.stringify(Json.toJson(BankAccount(false, None, None, None))))
        .vatScheme.doesNotExistForKey("bank-account")
        .vatScheme.contains(emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData)))
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Json.obj("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.bankdetails.routes.NoUKBankAccountController.show.url)
    }
  }

}
