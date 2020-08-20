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

package controllers

import fixtures.VatRegistrationFixture
import models.{TradingNameView, _}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class TradingDetailsControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new TradingDetailsController(
      messagesControllerComponents,
      mockKeystoreConnector,
      mockAuthClientConnector,
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

  "tradingNamePage" should {

    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(Some(TradingNameView(yesNo = true, Some("tradingName"))))))

      callAuthorised(testController.tradingNamePage) {
        result => {
          status(result) mustBe OK
          val doc = Jsoup.parse(contentAsString(result))

          doc.getElementById("tradingNameRadio-false").attr("checked") mustBe ""
          doc.getElementById("tradingNameRadio-true").attr("checked") mustBe "checked"
          doc.getElementById("tradingName").`val` mustBe "tradingName"
        }
      }
    }

    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))

      callAuthorised(testController.tradingNamePage) {
        result => {
          status(result) mustBe OK
          val doc = Jsoup.parse(contentAsString(result))
          doc.getElementById("tradingNameRadio-false").attr("checked") mustBe ""
          doc.getElementById("tradingNameRadio-true").attr("checked") mustBe ""
          doc.getElementById("tradingName").`val` mustBe ""
        }
      }
    }
  }

  "submitTradingName" should {

    val fakeRequest = FakeRequest(routes.TradingDetailsController.submitTradingName())

    "return 303 when they do not trade under a different name" in new Setup {
      when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> "false"
      )

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/trade-goods-outside-eu")
      }
    }

    "return 303 with a provided trading name" in new Setup {
      when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> "true",
        "tradingName" -> "some name"
      )

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/trade-goods-outside-eu")
      }
    }

    "return 400 without a provided trading name" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> "true",
        "tradingName" -> ""
      )

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when no option is selected" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when the trading name they have provided is invalid" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> "true",
        "tradingName" -> "$0M3 T3$T"
      )

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 400
      }
    }
  }

  "euGoodsPage" should {

    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(euGoods = Some(true))))

      callAuthorised(testController.euGoodsPage) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))

      callAuthorised(testController.euGoodsPage) {
        result => {
          status(result) mustBe OK
        }
      }
    }
  }

  "submitEuGoods" should {

    val fakeRequest = FakeRequest(routes.TradingDetailsController.submitEuGoods())

    "return 303 when they trade eu goods and redirect to the apply eori page" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> "true"
      )

      submitAuthorised(testController.submitEuGoods, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.chargeExpectancyPage.url)
      }
    }

    "return 303 when they don't trade eu goods and redirect to the estimated turnover page" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "euGoodsRadio" -> "false"
      )

      submitAuthorised(testController.submitEuGoods, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.ReturnsController.chargeExpectancyPage.url)
      }
    }

    "return 400 when no option is selected" in new Setup {
      when(mockTradingDetailsService.saveEuGoods(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submitEuGoods, request) { result =>
        status(result) mustBe 400
      }
    }

  }
}
