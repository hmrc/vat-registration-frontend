/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import models.api.{EligibilitySubmissionData, NETP, NonUkNonEstablished}
import models.{NonUk, TransferOfAGoingConcern}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class VatExemptionControllerISpec extends ControllerISpec {

  val url: String = routes.VatExemptionController.show.url
  val testReturns: Returns = Returns(Some(testTurnover))
  
  s"GET $url" must {
    "Return OK when there is no value for 'appliedForExemption' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns)
        .vatScheme.doesNotHave("returns")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "Return OK with prepop when there is a value for 'appliedForExemption' in the backend" in new Setup {
      given()
        .user.isAuthorised()
        .vatScheme.has("returns", Json.toJson(testReturns.copy(appliedForExemption = Some(false))))
        .s4lContainer[Returns].isEmpty

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "No"
      }
    }

    "Return OK with prepop when there is a value for 'appliedForExemption' in S4L" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns.copy(appliedForExemption = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }
  }

  s"POST $url" must {
    "redirect to the bank account page when the user is TOGC/COLE" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns)
        .s4lContainer[Returns].isUpdatedWith(testReturns.copy(appliedForExemption = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(registrationReason = TransferOfAGoingConcern)))

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
      }
    }

    "redirect to the bank account page when the user is non-NETP" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns)
        .s4lContainer[Returns].isUpdatedWith(testReturns.copy(appliedForExemption = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.bankdetails.routes.HasBankAccountController.show.url)
      }
    }

    "redirect to send goods overseas page when the user is NETP" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns)
        .s4lContainer[Returns].isUpdatedWith(testReturns.copy(appliedForExemption = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP, registrationReason = NonUk)))

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
      }
    }

    "redirect to send goods overseas page when the user is NonUkNoNEstablished" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testReturns)
        .s4lContainer[Returns].isUpdatedWith(testReturns.copy(appliedForExemption = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished, registrationReason = NonUk)))

      val res = buildClient(url).post(Json.obj("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SendGoodsOverseasController.show.url)
      }
    }

    "return a bad request if nothing is selected" in {
      given()
        .user.isAuthorised()

      val res = buildClient(url).post("")

      whenReady(res) { result =>
        result.status mustBe BAD_REQUEST
      }
    }
  }

}
