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
import itutil.ControllerISpec
import models.{BankAccount, Lock}
import models.api.{EligibilitySubmissionData, Individual, NonUkNonEstablished}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.Instant

class HasBankAccountControllerISpec extends ControllerISpec {

  val url = "/companys-bank-account"

  "GET /companys-bank-account" must {
    "return OK with a blank form if the vat scheme doesn't contain bank details" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
    }
    "return OK and render the CanProvideBankDetailsView when UseNewBarsVerify is enabled" in new Setup {
      enable(UseNewBarsVerify)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc: Document = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.title() mustBe "Can you provide bank or building society details for VAT repayments to the business? - Register for VAT - GOV.UK"
      disable(UseNewBarsVerify)
    }

    "return OK and render the HasBankAccountView when UseNewBarsVerify is disabled" in new Setup {
      disable(UseNewBarsVerify)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc: Document = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.title() mustBe "Are you able to provide bank or building society account details for the business? - Register for VAT - GOV.UK"
    }

    "return SEE_OTHER when the party type is NETP" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = Individual,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
    }
    "return SEE_OTHER when the party type is Non UK Company" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
    }
    "return OK with 'Yes' pre-populated from backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = true, None, None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'Yes' pre-populated from the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(emptyBankAccount))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
    "return OK with 'No' pre-populated from the backend" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccountNotProvidedNoReason))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
    "redirect to BankDetailsLockoutController when user is locked" in new Setup {
      given().user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[BankAccount](None)

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
    "redirect to the UK bank page if the user has a bank account and useNewBarsVerify switch is disabled" in new Setup {
      disable(UseNewBarsVerify)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)
        .registrationApi.replaceSection[BankAccount](BankAccount(isProvided = true, None, None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
    }

    "redirect to the reason for no bank account page if the user doesn't have a bank account" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)
        .registrationApi.replaceSection[BankAccount](BankAccount(isProvided = false, None, None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.NoUKBankAccountController.show.url)
    }

    "redirect to the choose account type page if the user has a bank account and useNewBarsVerify switch is enabled" in new Setup {
      enable(UseNewBarsVerify)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)
        .registrationApi.replaceSection[BankAccount](BankAccount(isProvided = true, None, None))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.ChooseAccountTypeController.show.url)
      disable(UseNewBarsVerify)
    }

    "return BAD_REQUEST and render the CanProvideBankDetailsView when form is empty and UseNewBarsVerify is enabled" in new Setup {
      enable(UseNewBarsVerify)
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
      Jsoup.parse(res.body).title() must include("Can you provide bank or building society details for VAT repayments to the business? - Register for VAT - GOV.UK")
      disable(UseNewBarsVerify)
    }

    "return BAD_REQUEST and render the HasBankAccountView when form is empty and UseNewBarsVerify is disabled" in new Setup {
      disable(UseNewBarsVerify)
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
      Jsoup.parse(res.body).title() must include("Are you able to provide bank or building society account details for the business? - Register for VAT - GOV.UK")
    }

    "return BAD_REQUEST if has_bank_account option not selected" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)
      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }

}
