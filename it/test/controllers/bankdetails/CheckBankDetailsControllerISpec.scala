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
import models.{BankAccount, FailedVerification, Lock}
import models.bars.BankAccountType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.Instant

class CheckBankDetailsControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/check-bank-details"

  "GET /check-bank-details" when {

    "UseNewBarsVerify is disabled" must {

      "redirect to HasBankAccountController" in new Setup {
        disable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HasBankAccountController.show.url)
      }
    }

    "UseNewBarsVerify is enabled" must {

      "redirect to HasBankAccountController when session is empty" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HasBankAccountController.show.url)
      }

      "redirect to UkBankAccountDetailsController when fromEnterDetails flag is missing" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(buildClient("/account-details").post(
          Map(
            "accountName"   -> testBankName,
            "accountNumber" -> testAccountNumber,
            "sortCode"      -> testSortCode
          )))

        val session: CacheMap = await(repo.get(sessionString)).getOrElse(CacheMap(sessionString, Map.empty))
        await(repo.upsert(CacheMap(sessionString, session.data + ("fromEnterDetails" -> Json.toJson(false)))))

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.UkBankAccountDetailsController.show.url)
      }

      "return OK and display bank details when session contains bank details and token" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(
          buildClient("/account-details").post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode,
              "rollNumber"    -> testRollNumber
            )))

        val res: WSResponse = await(buildClient(url).get())
        val doc: Document   = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.body().text() must include(testBankName)
        doc.body().text() must include(testAccountNumber)
        doc.body().text() must include(testSortCode)
        doc.body().text() must include(testRollNumber)
      }

      "redirect to BankDetailsLockoutController when user is locked" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(barsLockRepository.collection.insertOne(
          Lock(currentProfile.registrationId, failedAttempts = 3, lastAttemptedAt = Instant.now())
        ).toFuture())

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.BankDetailsLockoutController.show.url)
      }
    }
  }

  "POST /check-bank-details" when {

    "UseNewBarsVerify is disabled" must {

      "redirect to HasBankAccountController" in new Setup {
        disable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HasBankAccountController.show.url)
      }
    }

    "UseNewBarsVerify is enabled" must {

      "redirect to TaskList when BARS verification passes" in new Setup {
        enable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .bars
          .verifySucceeds(BankAccountType.Business)
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))))
          .registrationApi
          .replaceSection[BankAccount](
            BankAccount(
              isProvided = true,
              details = Some(testUkBankDetails),
              reason = None,
              bankAccountType = Some(BankAccountType.Business)
            ))
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(
          buildClient("/account-details").post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }

      "redirect to AccountDetailsNotVerifiedController when BARS verification fails once" in new Setup {
        enable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .bars
          .verifyFails(BankAccountType.Business, BAD_REQUEST)
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(
          buildClient("/account-details").post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AccountDetailsNotVerifiedController.show.url)
      }

      "redirect to BankDetailsLockoutController when BARS verification fails and user is locked out" in new Setup {
        enable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .bars
          .verifyFails(BankAccountType.Business, BAD_REQUEST)
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, Some(BankAccountType.Business))))
          .registrationApi
          .replaceSection[BankAccount](BankAccount(
            isProvided      = false,
            details         = None,
            reason          = Some(FailedVerification),
            bankAccountType = None
          ))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(barsLockRepository.collection.insertOne(
          Lock(currentProfile.registrationId, failedAttempts = 2, lastAttemptedAt = Instant.now())
        ).toFuture())

        await(buildClient("/account-details").post(
          Map(
            "accountName"   -> testBankName,
            "accountNumber" -> testAccountNumber,
            "sortCode"      -> testSortCode
          )))

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.BankDetailsLockoutController.show.url)
      }

      "redirect to HasBankAccountController when session is empty" in new Setup {
        enable(UseNewBarsVerify)
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HasBankAccountController.show.url)
      }

      "redirect to HasBankAccountController if there is no account type saved" in new Setup {
        enable(UseNewBarsVerify)
        given().user
          .isAuthorised()
          .registrationApi
          .getSection[BankAccount](Some(BankAccount(isProvided = true, None, None, bankAccountType = None)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        await(
          buildClient("/account-details").post(
            Map(
              "accountName"   -> testBankName,
              "accountNumber" -> testAccountNumber,
              "sortCode"      -> testSortCode
            )))

        val res: WSResponse = await(buildClient(url).post(Map.empty[String, String]))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HasBankAccountController.show.url)
      }
    }
  }
}
