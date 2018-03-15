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
import features.bankAccountDetails.controllers.routes
import features.bankAccountDetails.forms.EnterBankAccountDetailsForm._
import features.bankAccountDetails.forms.HasCompanyBankAccountForm.HAS_COMPANY_BANK_ACCOUNT_RADIO
import helpers.ClientHelper
import itutil.{IntegrationSpecBase, WiremockHelper}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

class BankAccountClientSpec extends IntegrationSpecBase with ClientHelper {

  override val mockHost: String = WiremockHelper.wiremockHost
  override val mockPort: Int = WiremockHelper.wiremockPort

  val userId = "user-id-12345"
  val regId = "reg-12345"

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  trait Setup {
    stubSuccessfulLogin(userId)
    stubKeystoreFetchCurrentProfile(sessionId, regId)
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
        |    "sortCode":"testSortCode",
        |    "number":"testNumber"
        |  }
        |}
        |""".stripMargin).as[JsObject]
  }

  val bankAccountNotProvidedJson: JsObject = Json.obj("isProvided" -> false)
  val bankAccountProvidedPartialJson: JsObject = Json.obj("isProvided" -> true)

  s"""GET ${routes.BankAccountDetailsController.showHasCompanyBankAccountView()}""" should {

    val url = routes.BankAccountDetailsController.showHasCompanyBankAccountView().url
    val client = buildClient(url)

    "return a 200 and render the page with nothing pre-popped when it's the first time on the page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubAuthWithAffinity(Organisation)

      val response: WSResponse = client.withSessionCookieHeader(userId).get()
      response.status shouldBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-true").attributes.hasKey("checked") shouldBe false
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-false").attributes.hasKey("checked") shouldBe false
    }

    "return a 200 and render the page with the 'yes' radio pre-popped from save4later" in new Setup {
      stubS4LFetchBankAccount(regId, Some(bankAccountProvidedJson))
      stubAuthWithAffinity(Organisation)

      val response: WSResponse = client.withSessionCookieHeader(userId).get()
      response.status shouldBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-true").attributes.hasKey("checked") shouldBe true
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-false").attributes.hasKey("checked") shouldBe false
    }

    "return a 200 and render the page with the 'yes' radio pre-popped from VAT Backend" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, Some(bankAccountProvidedJson))
      stubAuthWithAffinity(Organisation)

      val response: WSResponse = client.withSessionCookieHeader(userId).get()
      response.status shouldBe 200

      val document: Document = Jsoup.parse(response.body)
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-true").attributes.hasKey("checked") shouldBe true
      document.getElementById(s"$HAS_COMPANY_BANK_ACCOUNT_RADIO-false").attributes.hasKey("checked") shouldBe false
    }
  }

  s"""POST ${routes.BankAccountDetailsController.showHasCompanyBankAccountView()}""" should {

    val url = routes.BankAccountDetailsController.showHasCompanyBankAccountView().url
    val client = buildClient(url)

    "save the form data to save4later when 'yes' was selected and the rest of the bank account block is empty and return a 303" +
      "and redirect to the 'enter company bank details' page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubS4LSaveBankAccount(regId)
      stubAuthWithAffinity(Organisation)

      val formBody: JsObject = Json.obj(HAS_COMPANY_BANK_ACCOUNT_RADIO -> true)
      val response: WSResponse = client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody)

      response.status shouldBe 303
      redirectLocation(response) shouldBe Some(routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url)
    }

    "save the form data to VAT backend and S4L when 'no' was selected and return a 303 and redirect to the 'join frs' page" in new Setup {
      Given("There is no bank account details in S4L or VAT backend")
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)

      And("The user is logged in with an Organisation account")
      stubAuthWithAffinity(Organisation)

      And("The form data will be saved to both S4L and VAT backend")
      stubS4LSaveBankAccount(regId)
      stubVATPatchBankAccount(regId, bankAccountNotProvidedJson)

      When("The 'has a bank account' page is submitted with the 'no' radio button selected")
      val formBody: JsObject = Json.obj(HAS_COMPANY_BANK_ACCOUNT_RADIO -> false)
      val response: WSResponse = client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody)

      Then(s"The client is served a 303 and redirected to ${features.frs.controllers.routes.FlatRateController.joinFrsPage()}")
      response.status shouldBe 303
      redirectLocation(response) shouldBe Some(features.frs.controllers.routes.FlatRateController.joinFrsPage().url)
    }
  }

  s"""GET ${routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()}""" should {

    val url = routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url
    val client = buildClient(url)

    "return a 200 and render the page with nothing pre-popped when it's the first time on the page" in new Setup {
      stubS4LFetchBankAccount(regId, None)
      stubVATFetchBankAccount(regId, None)
      stubAuthWithAffinity(Organisation)

      val response: WSResponse = client.withSessionCookieHeader(userId).get()

      response.status shouldBe 200
    }
  }

  s"""POST ${routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails()}""" should {

    val url = routes.BankAccountDetailsController.showEnterCompanyBankAccountDetails().url
    val client = buildClient(url)

    "save the form data to S4L and VAT backend when 'has bank account' data is already in S4L," +
      "return a 303 and redirect to the 'join frs' page" in new Setup {
      Given("The 'has bank account' value is saved in S4L from the previous page")
      stubS4LFetchBankAccount(regId, Some(bankAccountProvidedPartialJson))
      stubAuthWithAffinity(Organisation)

      And("The bank details form data will be valid")
      stubBankReputationCheck(valid = true)

      And("The form data along with the 'has bank account' value from the previous page will be saved to S4L and VAT backend")
      stubS4LSaveBankAccount(regId)
      stubVATPatchBankAccount(regId, bankAccountProvidedJson)

      When("the 'enter bank details page' is submitted with valid data")
      val formBody: JsObject = Json.obj(
        ACCOUNT_NAME -> "test account name",
        ACCOUNT_NUMBER -> "12345678",
        SORT_CODE -> Json.obj("part1" -> "12", "part2" -> "34", "part3" -> "56")
      )
      val response: WSResponse = client.withSessionCookieHeader(userId).withCSRFTokenHeader.post(formBody)

      Then(s"the client is served a 303 response and is redirected to ${features.frs.controllers.routes.FlatRateController.joinFrsPage()}")
      response.status shouldBe 303
      redirectLocation(response) shouldBe Some(features.frs.controllers.routes.FlatRateController.joinFrsPage().url)
    }
  }
}
