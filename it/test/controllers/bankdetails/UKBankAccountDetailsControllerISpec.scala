/*
 * Copyright 2026 HM Revenue & Customs
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

import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.bars.BankAccountType.Personal
import models.{BankAccount, BankAccountDetails, Lock}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class UKBankAccountDetailsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/account-details"

  "GET /account-details" when {
    "return an OK and render the UKBankAccountDetails page" when {
      "the user has no existing data to pre-populate" in new Setup {
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("input[id=accountName]").`val`() mustBe ""
      }

      "the user has bank account details pre-populated from the backend" in new Setup {
        private val existingDetails = BankAccountDetails(testBankName, testAccountNumber, testSortCode, Some(testRollNumber))
        given().user.isAuthorised().registrationApi.getSection[BankAccount](Some(bankAccount.copy(details = Some(existingDetails))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.select("input[id=accountName]").`val`() mustBe testBankName
        doc.select("input[id=accountNumber]").`val`() mustBe testAccountNumber
        doc.select("input[id=sortCode]").`val`() mustBe testSortCode
        doc.select("input[id=rollNumber]").`val`() mustBe testRollNumber
      }
    }

    "return a redirect to the BankDetailsLockout page when user is locked" in new Setup {
      given().user.isAuthorised().registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      await(
        barsLockRepository.collection
          .insertOne(
            Lock(currentProfile.registrationId, failedAttempts = 3, lastAttemptedAt = Instant.now())
          )
          .toFuture())

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.BankDetailsLockoutController.show.url)
    }
  }

  "POST /account-details" when {
    "save bank details and redirect to the CheckBankDetails page" when {
      "a valid form is submitted without a roll call number" in new Setup {
        given().user
          .isAuthorised()
          .user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, Some(Personal))))
          .registrationApi
          .replaceSection[BankAccount](
            BankAccount(isProvided = true, Some(BankAccountDetails(testBankName, testAccountNumber, testSortCode, None)), None, Some(Personal)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(
          buildClient(url).post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckBankDetailsController.show.url)
      }

      "a valid form is submitted with a roll call number" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, Some(Personal))))
          .registrationApi
          .replaceSection[BankAccount](
            BankAccount(
              isProvided = true,
              Some(BankAccountDetails(testBankName, testAccountNumber, testSortCode, Some(testRollNumber))),
              None,
              Some(Personal)))

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
        res.header(HeaderNames.LOCATION) mustBe Some(routes.CheckBankDetailsController.show.url)
      }
    }

    "redirect to the first page in journey (HasBankAccount page) when no BankAccount data exists" in new Setup {
      given().user.isAuthorised().user.isAuthorised().registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(
        buildClient(url).post(
          Map(
            "accountName"   -> testBankName,
            "accountNumber" -> testAccountNumber,
            "sortCode"      -> testSortCode
          )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.CanYouProvideBankAccountDetailsController.show.url)
    }

    "return a BAD_REQUEST and re-render the page with errors" when {
      "the form fields are submitted empty" in new Setup {
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
        Jsoup.parse(res.body).title() must include(
          "Error: Can you provide banking details for VAT repayments to the business? - Register for VAT - GOV.UK")
      }

      "the form is submitted with an invalid account number" in new Setup {
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
        Jsoup.parse(res.body).title() must include(
          "Error: Can you provide banking details for VAT repayments to the business? - Register for VAT - GOV.UK")
      }
    }
  }

}
