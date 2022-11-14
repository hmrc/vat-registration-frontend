
package controllers.bankdetails

import featureswitch.core.config.TaskList
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.{BankAccount, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class UKBankAccountDetailsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/account-details"

  "GET /account-details" must {
    "return OK with a blank form if the VAT scheme doesn't contain bank details" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())

      res.status mustBe OK
    }
    "return OK with a pre-populated form from S4L" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccount))

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
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccount))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get())
      val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select("input[id=accountName]").`val`() mustBe testBankName
      doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
      doc.select("input[id=sortCode]").`val`() mustBe testSortCode
    }
  }

  "POST /account-details" when {
    "bank details are valid and the user is TOGC/COLE" must {
      "return to the returns frequency page" in new Setup {
        given
          .user.isAuthorised()
          .bankAccountReputation.passes
          .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
          .registrationApi.replaceSection[BankAccount](bankAccount)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(
          "accountName" -> testBankName,
          "accountNumber" -> testAccountNumber,
          "sortCode" -> testSortCode
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ReturnsFrequencyController.show.url)
      }
    }
    "bank details and Bank Account Reputation states are invalid" must {
      "return BAD_REQUEST" in new Setup {
        given
          .user.isAuthorised()
          .bankAccountReputation.fails
          .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(
          "accountName" -> testBankName,
          "accountNumber" -> testAccountNumber,
          "sortCode" -> testSortCode
        )))

        res.status mustBe BAD_REQUEST
      }
    }

    "redirect to the application-progress page if Tasklist FS is enabled" in new Setup {
      enable(TaskList)
      given
        .user.isAuthorised()
        .bankAccountReputation.passes
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
        .registrationApi.replaceSection[BankAccount](bankAccount)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "accountName" -> testBankName,
        "accountNumber" -> testAccountNumber,
        "sortCode" -> testSortCode
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      disable(TaskList)
    }

    "bank details are incorrect" must {
      "return BAD_REQUEST" in new Setup {
        given
          .user.isAuthorised()
          .bankAccountReputation.fails
          .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(
          "accountName" -> "",
          "accountNumber" -> "",
          "sortCode" -> ""
        )))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
