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

package controllers.vatapplication

import forms.PaymentFrequencyForm.monthly
import itutil.ControllerISpec
import models.api.vatapplication._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PaymentFrequencyControllerISpec extends ControllerISpec {

  val url: String = routes.PaymentFrequencyController.show.url
  val testFullVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(1000),
    northernIrelandProtocol = None,
    claimVatRefunds = Some(true),
    appliedForExemption = None,
    overseasCompliance = None,
    startDate = testIncorpDate,
    returnsFrequency = Some(Annual),
    staggerStart = Some(FebJanStagger),
    annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO))),
    hasTaxRepresentative = None
  )

  s"GET $url" must {
    "return an OK with no prepop data" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an OK when there is data to prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[VatApplication](Some(testFullVatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST $url" must {
    "return a redirect to next page and update backend" in new Setup {
      val vatApplication: VatApplication = fullVatApplication.copy(returnsFrequency = Some(Annual))
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[VatApplication](vatApplication.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment)))))
        .registrationApi.getSection[VatApplication](Some(vatApplication.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment))))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> monthly))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PaymentMethodController.show.url)
      }
    }

    "return a redirect to next page and update backend with full model" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection(testFullVatApplication)
        .registrationApi.replaceSection[VatApplication](testFullVatApplication)
        .registrationApi.getSection[VatApplication](Some(testFullVatApplication))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> monthly))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PaymentMethodController.show.url)
      }
    }

    "return a bad request and update page with errors on an invalid submission" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}