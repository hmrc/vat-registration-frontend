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

package controllers.registration.business

import fixtures.VatRegistrationFixture
import models.{TradingDetails, TradingNameView}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.soletrader_name

import scala.concurrent.Future

class MandatoryTradingNameControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val fakeRequest = FakeRequest(controllers.registration.business.routes.MandatoryTradingNameController.show())

  class Setup {
    val view = app.injector.instanceOf[soletrader_name]
    val testController = new MandatoryTradingNameController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockApplicantDetailsServiceOld,
      mockTradingDetailsService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val tradingName = "Test Trader"
  val tradingNameViewNo = TradingNameView(yesNo = false, None)
  val fullS4L = TradingDetails(
    Some(tradingNameViewNo),
    Some(true)
  )

  "show" should {
    "return an Ok when there is trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(Some(TradingNameView(yesNo = true, Some("tradingName"))))))
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(tradingName))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }
  }

  "return an Ok when there is no trading details present" in new Setup {
    when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
      .thenReturn(Future.successful(TradingDetails()))
    when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
      .thenReturn(Future.successful(tradingName))

    callAuthorised(testController.show) {
      result => {
        status(result) mustBe OK
      }
    }
  }

  "return an Ok when there is a company name present" in new Setup {
    when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
      .thenReturn(Future.successful(tradingName))

    callAuthorised(testController.show) {
      result => {
        status(result) mustBe OK
      }
    }
  }

  "submit" should {
    "return 303 with a provided trading name" in new Setup {
      when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(tradingName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "trading-name" -> "Test Trader"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/apply-for-eori")
      }
    }

    "return 400 without a provided trading name" in new Setup {
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(tradingName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "trading-name" -> ""
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when trading name is empty" in new Setup {
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(tradingName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when the trading name they have provided is invalid" in new Setup {
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(tradingName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "trading-name" -> "$0M3 T3$T"
      )

      submitAuthorised(testController.submit, request) { result =>
        status(result) mustBe 400
      }
    }
  }

}
