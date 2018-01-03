/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.vatTradingDetails

import controllers.vatTradingDetails
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.view.vatTradingDetails.TradingNameView
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class TradingNameControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {
  object TestTradingNameController extends TradingNameController(
    ds,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService,
    mockVatRegistrationService
  )

  val fakeRequest = FakeRequest(vatTradingDetails.routes.TradingNameController.show())

  s"GET ${vatTradingDetails.routes.TradingNameController.show()}" should {
    "return HTML when there's a trading name in S4L" in {
      val tradingName = TradingNameView(TradingNameView.TRADING_NAME_YES, Some("Test Trading Name"))

      save4laterReturnsViewModel(tradingName)()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestTradingNameController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Trading name")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNoViewModel[TradingNameView]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestTradingNameController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Trading name")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNoViewModel[TradingNameView]()

      mockGetCurrentProfile()

      when(mockVatRegistrationService.getVatScheme(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestTradingNameController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Trading name")
      }
    }
  }

  s"POST ${vatTradingDetails.routes.TradingNameController.submit()} with Empty data" should {
    "return 400" in {

      mockGetCurrentProfile()

      submitAuthorised(TestTradingNameController.submit(), fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }
    }
  }

  s"POST ${vatTradingDetails.routes.TradingNameController.submit()} with valid data no trading name" should {
    "return 303" in {
      save4laterExpectsSave[TradingNameView]()

      mockGetCurrentProfile()

      submitAuthorised(TestTradingNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> TradingNameView.TRADING_NAME_NO
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/describe-what-company-does"
      }
    }
  }

  s"POST ${vatTradingDetails.routes.TradingNameController.submit()} with valid data with trading name" should {
    "return 303" in {
      save4laterExpectsSave[TradingNameView]()

    mockGetCurrentProfile()

      submitAuthorised(TestTradingNameController.submit(), fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> TradingNameView.TRADING_NAME_YES,
        "tradingName" -> "some name"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/describe-what-company-does"
      }
    }
  }
}
