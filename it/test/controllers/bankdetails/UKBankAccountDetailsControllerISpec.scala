/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.bankdetails

import featuretoggle.FeatureSwitch.UseNewBarsVerify
import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.{BankAccount, TransferOfAGoingConcern}
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class UKBankAccountDetailsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/account-details"

  "GET /account-details" when {

    "UseNewBarsVerify is disabled" must {

      "return OK with a blank form if the VAT scheme doesn't contain bank details" in new Setup {
        disable(UseNewBarsVerify)
        given().user.isAuthorised().registrationApi.getSection[BankAccount](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("input[id=accountName]").size() mustBe 1
        doc.select("input[id=accountNumber]").size() mustBe 1
        doc.select("input[id=sortCode]").size() mustBe 1
        doc.select("input[id=rollNumber]").size() mustBe 0
      }

      "return OK with a form pre-populated from the backend when bank details exist" in new Setup {
        disable(UseNewBarsVerify)
        given().user.isAuthorised().registrationApi.getSection[BankAccount](Some(bankAccount))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("input[id=accountName]").`val`() mustBe testBankName
        doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
        doc.select("input[id=sortCode]").`val`() mustBe testSortCode
        doc.select("input[id=rollNumber]").size() mustBe 0
      }
    }

    "UseNewBarsVerify is enabled" must {

      "return OK with a blank form when session is empty" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        res.status mustBe OK
        val doc: Document = Jsoup.parse(res.body)
        doc.select("input[id=accountName]").`val`() mustBe ""
      }

      "return OK with a form pre-populated from session when session contains bank details" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode,
              "rollNumber"    -> testRollNumber
            )))

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("input[id=accountName]").`val`() mustBe testBankName
        doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
        doc.select("input[id=sortCode]").`val`() mustBe testSortCode
        doc.select("input[id=rollNumber]").`val`() mustBe testRollNumber
      }
    }
  }

  "POST /account-details" when {

    "UseNewBarsVerify is disabled" must {

      "redirect to the Task List when valid bank details" in new Setup {
        disable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .bankAccountReputation
          .passes
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
          .registrationApi
          .replaceSection[BankAccount](bankAccount)
          .registrationApi
          .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }

      "return BAD_REQUEST when valid bank details fail BARS verification" in new Setup {
        disable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .bankAccountReputation
          .fails
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
          .registrationApi
          .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        res.status mustBe BAD_REQUEST
      }

      "return BAD_REQUEST when form fields are empty" in new Setup {
        disable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> "",
              "accountNumber" -> "",
              "sortCode"      -> ""
            )))

        res.status mustBe BAD_REQUEST
      }
    }

    "UseNewBarsVerify is enabled" must {

      "save bank details to session and redirect to Task List when form is valid" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }

      "save bank details including roll number to session and redirect to Task List" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode,
              "rollNumber"    -> testRollNumber
            )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }

      "return BAD_REQUEST without calling BARS when form fields are empty" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> "",
              "accountNumber" -> "",
              "sortCode"      -> ""
            )))

        res.status mustBe BAD_REQUEST
      }

      "return BAD_REQUEST without calling BARS when account number is invalid" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> "invalid",
              "sortCode"      -> testSortCode
            )))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
