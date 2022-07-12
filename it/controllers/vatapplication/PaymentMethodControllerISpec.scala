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

package controllers.vatapplication

import featureswitch.core.config.TaxRepPage
import forms.PaymentMethodForm._
import itutil.ControllerISpec
import models.api.vatapplication._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PaymentMethodControllerISpec extends ControllerISpec {

  val url: String = routes.PaymentMethodController.show.url
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
        .s4lContainer[VatApplication].contains(VatApplication(returnsFrequency = Some(Annual)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an OK when there is data to prepop in s4l" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Bank Giro Transfer"
      }
    }

    "return an OK when there is data to prepop in BE" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].isEmpty
        .registrationApi.getSection(Some(VatApplication(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(CHAPS))))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Clearing House Automated Payment System (CHAPS)"
      }
    }
  }

  s"POST $url" must {
    "return a redirect to the Join Flat Rate page and update S4L" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment)))))
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(StandingOrder)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> standingOrder))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }

    "return a redirect to the Join Flat Rate page and update backend with full model" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(testFullVatApplication)
        .s4lContainer[VatApplication].clearedByKey
        .registrationApi.replaceSection(testFullVatApplication.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(CHAPS)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> chaps))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }

    "return a redirect to the Tax Representative page if the feature switch is on" in new Setup {
      enable(TaxRepPage)

      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(testFullVatApplication)
        .s4lContainer[VatApplication].clearedByKey
        .registrationApi.replaceSection(testFullVatApplication.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BACS)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> bacs))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.TaxRepController.show.url)
      }
    }

    "return a bad request and update page with errors on an invalid submission" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(returnsFrequency = Some(Annual)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
