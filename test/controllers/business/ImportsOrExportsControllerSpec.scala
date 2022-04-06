/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.business

import fixtures.VatRegistrationFixture
import models.{TradingNameView, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.business.ImportsOrExports

import scala.concurrent.Future

class ImportsOrExportsControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new ImportsOrExportsController(
      mockAuthClientConnector,
      mockSessionService,
      mockTradingDetailsService,
      app.injector.instanceOf[ImportsOrExports]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val companyName = "Test Company Name Ltd"
  val tradingNameViewNo: TradingNameView = TradingNameView(yesNo = false, None)
  val fullS4L: TradingDetails = TradingDetails(
    Some(tradingNameViewNo),
    Some(true)
  )

  "show" should {
    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(tradeVatGoodsOutsideUk = Some(true))))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }
  }

  "submit" should {
    val fakeRequest = FakeRequest(controllers.business.routes.ImportsOrExportsController.submit)

    "return 303 when tradeVatGoodsOutsideUk is true and redirect to the apply for eori page" in new Setup {
      when(mockTradingDetailsService.saveTradeVatGoodsOutsideUk(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.business.routes.ApplyForEoriController.show.url)
      }
    }

    "return 303 when tradeVatGoodsOutsideUk is false and redirect to the zero rated resolver" in new Setup {
      when(mockTradingDetailsService.saveTradeVatGoodsOutsideUk(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "value" -> "false"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.returns.routes.ZeroRatedSuppliesResolverController.resolve.url)
      }
    }

    "return 400 when no option is selected" in new Setup {
      when(mockTradingDetailsService.saveTradeVatGoodsOutsideUk(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }
  }
}
