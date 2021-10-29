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
import models.api.{NonUkNonEstablished, UkCompany}
import models.{CurrentProfile, TradingDetails, TradingNameView}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.mocks.TimeServiceMock
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.trading_name

import scala.concurrent.Future

class TradingNameControllerSpec extends ControllerSpec with VatRegistrationFixture with TimeServiceMock with FutureAssertions {
  val fakeRequest = FakeRequest(controllers.registration.business.routes.TradingNameController.show)

  class Setup {
    val view = app.injector.instanceOf[trading_name]
    val testController = new TradingNameController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockApplicantDetailsServiceOld,
      mockTradingDetailsService,
      mockVatRegistrationService,
      view
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


  "show" should {
    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(Some(TradingNameView(yesNo = true, Some("tradingName"))))))
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(Some(companyName)))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }
    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(Some(companyName)))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }
    "return an Ok when there is a company name present" in new Setup {
      when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
        .thenReturn(Future.successful(Some(companyName)))

      callAuthorised(testController.show) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "submit" should {
      "return 303 when they do not trade under a different name" in new Setup {
        when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(fullS4L))
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))
        when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
          .thenReturn(Future.successful(UkCompany))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> "false"
        )

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some("/register-for-vat/apply-for-eori")
        }
      }

      "return 303 with a provided trading name" in new Setup {
        when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(fullS4L))
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))
        when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
          .thenReturn(Future.successful(UkCompany))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> "true",
          "tradingName" -> "some name"
        )

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some("/register-for-vat/apply-for-eori")
        }
      }

      "return 303 with a provided trading name and redirect to zero rated turnover for Non UK Company" in new Setup {
        when(mockTradingDetailsService.saveTradingName(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(fullS4L))
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))
        when(mockVatRegistrationService.partyType(any[CurrentProfile], any[HeaderCarrier]))
          .thenReturn(Future.successful(NonUkNonEstablished))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> "true",
          "tradingName" -> testTradingName
        )

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 303
          redirectLocation(result) mustBe Some(controllers.registration.returns.routes.ZeroRatedSuppliesResolverController.resolve().url)
        }
      }

      "return 400 without a provided trading name" in new Setup {
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> "true",
          "tradingName" -> ""
        )

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 400
        }
      }

      "return 400 when no option is selected" in new Setup {
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 400
        }
      }

      "return 400 when the trading name they have provided is invalid" in new Setup {
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(companyName)))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
          "value" -> "true",
          "tradingName" -> "$0M3 T3$T"
        )

        submitAuthorised(testController.submit, request) { result =>
          status(result) mustBe 400
        }
      }

      "return an exception when there is no company name present" in new Setup {
        when(mockApplicantDetailsServiceOld.getCompanyName(any(), any()))
          .thenReturn(Future(throw exception))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

        submitAuthorised(testController.submit, request) {
          _ failedWith exception
        }
      }
    }
  }

}
