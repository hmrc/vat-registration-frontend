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

package controllers.registration.business

import fixtures.VatRegistrationFixture
import models.{TradingNameView, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class EuGoodsControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new EuGoodsController(
      messagesControllerComponents,
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockApplicantDetailsServiceOld,
      mockTradingDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val companyName = "Test Company Name Ltd"
  val tradingNameViewNo = TradingNameView(yesNo = false, None)
  val fullS4L = TradingDetails(
    Some(tradingNameViewNo),
    Some(true)
  )

  "euGoodsPage" should {

    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(euGoods = Some(true))))

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

  "submitEuGoods" should {

    val fakeRequest = FakeRequest(controllers.registration.business.routes.EuGoodsController.submit())

    "return 303 when they trade eu goods and redirect to the zero rated supplies page" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> "true"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.ZeroRatedSuppliesController.show().url)
      }
    }

    "return 303 when they don't trade eu goods and redirect to the zero rated supplies page" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> "false"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.ZeroRatedSuppliesController.show().url)
      }
    }

    "return 400 when no option is selected" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }

  }
}
