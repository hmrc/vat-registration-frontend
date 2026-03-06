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

import featuretoggle.FeatureSwitch.UseNewBarsVerify
import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.BankAccount
import models.bars.BankAccountType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class ChooseAccountTypeControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/choose-account-type"

  "GET /choose-account-type" must {
    "redirect to HasBankAccountController when feature switch is disabled" in new Setup {
      disable(UseNewBarsVerify)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
      enable(UseNewBarsVerify)
    }

    "return OK with a blank form if the VAT scheme doesn't contain bank account type" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())

      res.status mustBe OK
    }

    "return OK with a pre-populated form when bankAccountType is Business" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccount.copy(bankAccountType = Some(BankAccountType.Business))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc: Document   = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select(s"input[value=${BankAccountType.Business.asBars}]").first().hasAttr("checked") mustBe true
    }

    "return OK with a pre-populated form when bankAccountType is Personal" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(bankAccount.copy(bankAccountType = Some(BankAccountType.Personal))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get())
      val doc: Document   = Jsoup.parse(res.body)

      res.status mustBe OK
      doc.select(s"input[value=${BankAccountType.Personal.asBars}]").first().hasAttr("checked") mustBe true
    }
  }

  "POST /choose-account-type" when {
    "a valid Business account type is submitted" must {
      "redirect to the UK bank account details page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[BankAccount](Some(bankAccount))
          .registrationApi.replaceSection[BankAccount](bankAccount.copy(bankAccountType = Some(BankAccountType.Business)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map("value" -> BankAccountType.Business.asBars)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
      }
    }

    "a valid Personal account type is submitted" must {
      "redirect to the UK bank account details page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[BankAccount](Some(bankAccount))
          .registrationApi.replaceSection[BankAccount](bankAccount.copy(bankAccountType = Some(BankAccountType.Personal)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map("value" -> BankAccountType.Personal.asBars)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.UkBankAccountDetailsController.show.url)
      }
    }

    "no option is selected" must {
      "return BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[BankAccount](Some(bankAccount))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map("value" -> "")))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}