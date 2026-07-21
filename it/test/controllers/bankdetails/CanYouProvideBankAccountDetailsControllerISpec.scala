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

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Individual, NonUkNonEstablished}
import models.{BankAccount, Lock}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.Instant

class CanYouProvideBankAccountDetailsControllerISpec extends ControllerISpec {

  val url = "/companys-bank-account"

  "GET /companys-bank-account" must {
    "return an OK and render the CanYouProvideBankDetailsDetails page" when {
      "the user has no existing data to pre-populate" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi
          .getSection[BankAccount](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.title() mustBe "Can you provide banking details for VAT repayments to the business? - Register for VAT - GOV.UK"
      }

      "the user has answer 'Yes' pre-populated from the backend" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
          .registrationApi
          .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=true]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[value=false]").hasAttr("checked") mustBe false
      }

      "the user has answer 'No' pre-populated from the backend" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = false, None, None)))
          .registrationApi
          .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=true]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=false]").hasAttr("checked") mustBe true
      }
    }

    "return a redirect to the JoinFlatRateScheme page" when {
      "the user's party type is NETP" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[EligibilitySubmissionData](
            Some(
              testEligibilitySubmissionData.copy(
                partyType = Individual,
                fixedEstablishmentInManOrUk = false
              )))
          .registrationApi
          .getSection[BankAccount](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header("location") mustBe Some("/register-for-vat/join-flat-rate")
      }

      "the user's party type is Non UK Company" in new Setup {
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[EligibilitySubmissionData](
            Some(
              testEligibilitySubmissionData.copy(
                partyType = NonUkNonEstablished,
                fixedEstablishmentInManOrUk = false
              )))
          .registrationApi
          .getSection[BankAccount](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header("location") mustBe Some("/register-for-vat/join-flat-rate")
      }
    }

    "return a redirect to the BankDetailsLockout page when user is locked" in new Setup {
      given().user
        .isAuthorised()
        .registrationApi
        .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi
        .getSection[BankAccount](None)

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

  "POST /companys-bank-account" must {
    "redirect to the ChooseAccountType page if the user answers 'Yes'" in new Setup {
      given().user
        .isAuthorised()
        .registrationApi
        .getSection[BankAccount](None)
        .registrationApi
        .replaceSection[BankAccount](BankAccount(isProvided = true, None, None))
        .registrationApi
        .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.ChooseAccountTypeController.show().url)
    }

    "redirect to the NoUKBankAccount page if the user answers 'No'" in new Setup {
      given().user
        .isAuthorised()
        .registrationApi
        .getSection[BankAccount](None)
        .registrationApi
        .replaceSection[BankAccount](BankAccount(isProvided = false, None, None))
        .registrationApi
        .getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.NoUKBankAccountController.show().url)
    }

    "return a BAD_REQUEST and re-render the page with errors when the form is submitted empty" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
      Jsoup.parse(res.body).title() must include(
        "Error: Can you provide banking details for VAT repayments to the business? - Register for VAT - GOV.UK")
    }
  }

}
