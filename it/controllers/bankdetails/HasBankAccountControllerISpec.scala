
package controllers.bankdetails

import itutil.ControllerISpec
import models.BankAccount
import models.api.{EligibilitySubmissionData, NETP, NonUkNonEstablished}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class HasBankAccountControllerISpec extends ControllerISpec {

  val url = "/companys-bank-account"

  "GET /companys-bank-account" must {
    "return OK with a blank form if the vat scheme doesn't contain bank details" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
    "return SEE_OTHER when the party type is NETP" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
    }
    "return SEE_OTHER when the party type is Non UK Company" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
    }
    "return OK with 'Yes' pre-populated from S4L" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(true, None, None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'Yes' pre-populated from the backend" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(emptyBankAccount))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from S4L" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(false, None, None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from the backend" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccountNotProvidedNoReason))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
  }

  "POST /companys-bank-account" must {
    "redirect to the UK bank page if the user has a bank account" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)
        .registrationApi.replaceSection[BankAccount](BankAccount(true, None, None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
    }
    "redirect to the reason for no bank account page if the user doesn't have a bank account" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)
        .registrationApi.replaceSection[BankAccount](BankAccount(false, None, None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.NoUKBankAccountController.show.url)
    }

    "return BAD_REQUEST if has_bank_account option not selected" in new Setup {
      given.user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)
      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }

}
