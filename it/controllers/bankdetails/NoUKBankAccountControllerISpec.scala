/*
 * Copyright 2020 HM Revenue & Customs
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

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.{BankAccount, BeingSetupOrNameChange, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

class NoUKBankAccountControllerISpec extends ControllerISpec {

  val url: String = routes.NoUKBankAccountController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None, None)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "return an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None, Some(BeingSetupOrNameChange))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "beingSetup"
      }
    }
  }

  s"POST $url" must {
    "redirect to returns frequency page when the user is TOGC/COLE" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None, None)))
        .registrationApi.replaceSection[BankAccount](bankAccountNotProvided)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> "beingSetup"))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.ReturnsController.returnsFrequencyPage.url)
      }
    }

    "redirect to the start date resolver page when the user is non-NETP" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None, None)))
        .registrationApi.replaceSection[BankAccount](bankAccountNotProvided)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> "beingSetup"))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.VatRegStartDateResolverController.resolve.url)
      }
    }

    "redirect to the application-progress page if Tasklist FS is enabled" in new Setup {
      enable(TaskList)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[BankAccount](Some(BankAccount(isProvided = false, None, None, None)))
        .registrationApi.replaceSection[BankAccount](bankAccountNotProvided)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> "beingSetup"))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
      disable(TaskList)
    }

    "return BAD_REQUEST if no valid reason selected" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe 400
      }
    }
  }
}
