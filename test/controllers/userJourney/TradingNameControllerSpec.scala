/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.userJourney

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.TradingName
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class TradingNameControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestTradingNameController extends TradingNameController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.TradingNameController.show())

  s"GET ${routes.TradingNameController.show()}" should {

    "return HTML when there's a trading name in S4L" in {
      val tradingName = TradingName(TradingName.TRADING_NAME_YES, Some("Test Trading Name"))

      when(mockS4LService.fetchAndGet[TradingName]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(tradingName)))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestTradingNameController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Trading name")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[TradingName]()(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      callAuthorised(TestTradingNameController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Trading name")
      }
    }
  }

  s"POST ${routes.TradingNameController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestTradingNameController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.TradingNameController.submit()} with valid data no trading name" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(TradingName())))

      when(mockS4LService.saveForm[TradingName](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestTradingNameController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> TradingName.TRADING_NAME_NO
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe "/vat-registration/company-bank-account"
      }

    }
  }

  s"POST ${routes.TradingNameController.submit()} with valid data with trading name" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(TradingName())))

      when(mockS4LService.saveForm[TradingName](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestTradingNameController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> TradingName.TRADING_NAME_YES,
        "tradingName" -> "some name"
      )) {
        result =>
          status(result) mustBe Status.SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe "/vat-registration/company-bank-account"

      }

    }
  }

}
