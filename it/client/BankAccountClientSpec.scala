/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package client

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import controllers.routes
import fixtures.ITRegistrationFixtures
import forms.EnterBankAccountDetailsForm._
import forms.HasCompanyBankAccountForm.HAS_COMPANY_BANK_ACCOUNT_RADIO
import helpers.ClientHelper
import itutil.IntegrationSpecBase
import models.api.UkCompany
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class BankAccountClientSpec extends IntegrationSpecBase with AppAndStubs with ClientHelper with ITRegistrationFixtures {
  val regId = "1"

  val userId = "user-id-12345"

  class Setup {
    stubSuccessfulLogin(userId)

    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo: SessionRepository = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5.seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  def stubS4LFetchBankAccount(regId: String, response: Option[JsObject])(implicit app: Application): StubMapping =
    stubS4LFetch(regId, "bankAccount", response)

  def stubS4LSaveBankAccount(regId: String): StubMapping = stubS4LSave(regId, "bankAccount")

  val bankAccountProvidedJson: JsObject = {
    Json.parse(
      """
        |{
        |  "isProvided":true,
        |  "details":{
        |    "name":"testName",
        |    "number":"testNumber",
        |    "sortCode":"testSortCode"
        |  }
        |}
        |""".stripMargin).as[JsObject]
  }

  val bankAccountNotProvidedJson: JsObject = Json.obj("isProvided" -> false)
  val bankAccountProvidedPartialJson: JsObject = Json.obj("isProvided" -> true)

  s"""GET ${controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView()}""" should {

    val url = controllers.routes.BankAccountDetailsController.showHasCompanyBankAccountView().url
    val client = buildClient(url)

    "return a 200 and render the page with nothing pre-popped when it's the first time on the page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubAuthWithAffinity(Organisation)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(client.withSessionCookieHeader(userId).get())
      response.status mustBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO").attributes.hasKey("checked") mustBe false
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-no").attributes.hasKey("checked") mustBe false
    }

    "return a 200 and render the page with the 'yes' radio pre-popped from save4later" in new Setup {
      stubS4LFetchBankAccount(regId, Some(bankAccountProvidedJson))
      stubAuthWithAffinity(Organisation)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(client.withSessionCookieHeader(userId).get())
      response.status mustBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO").attributes.hasKey("checked") mustBe true
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-no").attributes.hasKey("checked") mustBe false
    }

    "return a 200 and render the page with the 'yes' radio pre-popped from VAT Backend" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, Some(bankAccountProvidedJson))
      stubAuthWithAffinity(Organisation)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(client.withSessionCookieHeader(userId).get())
      response.status mustBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO").attributes.hasKey("checked") mustBe true
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-no").attributes.hasKey("checked") mustBe false
    }
  }

  s"""POST ${routes.BankAccountDetailsController.showHasCompanyBankAccountView()}""" should {

    val url = routes.BankAccountDetailsController.showHasCompanyBankAccountView().url
    val client = buildClient(url)

    "save the form data to save4later when 'yes' was selected and the rest of the bank account block is empty and return a 303 " +
      "and redirect to the 'enter company bank details' page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubS4LSaveBankAccount(regId)
      stubAuthWithAffinity(Organisation)
      given()
        .vatScheme.contains(vatReg.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val formBody: JsObject = Json.obj(HAS_COMPANY_BANK_ACCOUNT_RADIO -> true)
      val response: WSResponse = await(client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody))

      response.status mustBe 303
      redirectLocation(response) mustBe Some(routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url)
    }

    "save the form data to VAT backend and S4L when 'no' was selected and return a 303 and redirect to the 'join frs' page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubAuthWithAffinity(Organisation)
      insertCurrentProfileIntoDb(currentProfile, sessionId)
      stubS4LSaveBankAccount(regId)
      stubVATPatchBankAccount(regId, bankAccountNotProvidedJson)
      given()
        .vatScheme.contains(vatReg.copy(eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))))
      val formBody: JsObject = Json.obj(HAS_COMPANY_BANK_ACCOUNT_RADIO -> false)
      val response: WSResponse = await(client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody))

      Then(s"The client is served a 303 and redirected to ${controllers.routes.NoUKBankAccountController.showNoUKBankAccountView()}")
      response.status mustBe 303
      redirectLocation(response) mustBe Some(controllers.routes.NoUKBankAccountController.showNoUKBankAccountView().url)
    }
  }

  s"""GET ${routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()}""" should {

    val url = routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url
    val client = buildClient(url)

    "return a 200 and render the page with nothing pre-popped when it's the first time on the page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubAuthWithAffinity(Organisation)
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(client.withSessionCookieHeader(userId).get())

      response.status mustBe 200
    }
  }

  s"""POST ${routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()}""" should {

    val url = routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url
    val client = buildClient(url)

    "save the form data to S4L and VAT backend when 'has bank account' data is already in S4L," +
      "return a 303 and redirect to the 'join frs' page" in new Setup {
      Given("The 'has bank account' value is saved in S4L from the previous page")
      stubS4LFetchBankAccount(regId, Some(bankAccountProvidedPartialJson))
      stubAuthWithInternalId("testInternalId")
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      And("The bank details form data will be valid")
      stubBankReputationCheck(valid = "yes")

      And("The form data along with the 'has bank account' value from the previous page will be saved to S4L and VAT backend")
      stubS4LSaveBankAccount(regId)
      stubVATPatchBankAccount(regId, bankAccountProvidedJson)

      When("the 'enter bank details page' is submitted with valid data")
      val formBody: JsObject = Json.obj(
        ACCOUNT_NAME -> "test account name",
        ACCOUNT_NUMBER -> "12345678",
        SORT_CODE -> "123456"
      )
      val response: WSResponse = await(client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody))

      Then(s"the client is served a 303 response and is redirected to ${controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show()}")
      response.status mustBe 303
      redirectLocation(response) mustBe Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show().url)
    }
  }
}
