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

package controllers.returns

import featureswitch.core.config.TaxRepPage
import forms.PaymentMethodForm._
import itutil.ControllerISpec
import models.api.returns._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PaymentMethodControllerISpec extends ControllerISpec {

  val url: String = routes.PaymentMethodController.show.url
  val testFullReturns: Returns = Returns(Some(testTurnover), None, Some(1000), Some(true), Some(Annual), Some(FebJanStagger), testIncorpDate, Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO))))

  s"GET $url" must {
    "return an OK with no prepop data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(returnsFrequency = Some(Annual)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an OK when there is data to prepop" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST $url" must {
    "return a redirect to the Join Flat Rate page and update S4L" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment)))))
        .s4lContainer[Returns].isUpdatedWith(Returns(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BACS)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> bacs))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }

    "return a redirect to the Join Flat Rate page and update backend with full model" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testFullReturns)
        .s4lContainer[Returns].clearedByKey
        .vatRegistration.storesReturns(testRegId, testFullReturns)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> bacs))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }

    "return a redirect to the Tax Representative page if the feature switch is on" in new Setup {
      enable(TaxRepPage)

      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(testFullReturns)
        .s4lContainer[Returns].clearedByKey
        .vatRegistration.storesReturns(testRegId, testFullReturns)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> bacs))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.returns.routes.TaxRepController.show.url)
      }
    }

    "return a bad request and update page with errors on an invalid submission" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(returnsFrequency = Some(Annual)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
