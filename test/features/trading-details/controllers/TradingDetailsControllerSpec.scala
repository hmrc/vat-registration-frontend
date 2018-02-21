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

package controllers

import connectors.KeystoreConnect
import features.tradingDetails.{TradingNameView, _}
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.IncorporationInformationService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class TradingDetailsControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {
  val mockTradingDetailsService: TradingDetailsServiceImpl = mock[TradingDetailsServiceImpl]

  class Setup {
    val testController = new TradingDetailsController {
      override val tradingDetailsService: TradingDetailsServiceImpl = mockTradingDetailsService
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val incorpInfoService: IncorporationInformationService = mockIncorpInfoService
      val authConnector: AuthConnector = mockAuthClientConnector
      val messagesApi: MessagesApi = mockMessagesAPI
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val companyName = "Test Company Name Ltd"
  val tradingNameViewNo = TradingNameView(yesNo = false, None)
  val fullS4L = TradingDetails(
    Some(tradingNameViewNo),
    Some(true),
    Some(false)
  )

  "tradingNamePage" should {

    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(Some(TradingNameView(yesNo = true, Some("tradingName"))))))

      when(mockIncorpInfoService.getCompanyName(any(), any())(any()))
          .thenReturn(Future.successful(companyName))

      callAuthorised(testController.tradingNamePage) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))

      when(mockIncorpInfoService.getCompanyName(any(), any())(any()))
        .thenReturn(Future.successful(companyName))

      callAuthorised(testController.tradingNamePage) {
        result => {
          status(result) mustBe OK
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
        redirectLocation(result) mustBe Some("/register-for-vat/trade-goods-services-with-countries-outside-uk")
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
        redirectLocation(result) mustBe Some("/register-for-vat/trade-goods-services-with-countries-outside-uk")
      }
    }

    "return 400 without a provided trading name" in new Setup {
      when(mockIncorpInfoService.getCompanyName(any(), any())(any()))
        .thenReturn(Future.successful(companyName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "tradingNameRadio" -> "true",
        "tradingName" -> ""
      )

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when no option is selected" in new Setup {
      when(mockIncorpInfoService.getCompanyName(any(), any())(any()))
        .thenReturn(Future.successful(companyName))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submitTradingName, request) { result =>
        status(result) mustBe 400
      }
    }

    "return 400 when the trading name they have provided is invalid" in new Setup {
      when(mockIncorpInfoService.getCompanyName(any(), any())(any()))
        .thenReturn(Future.successful(companyName))

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
        redirectLocation(result) mustBe Some("/register-for-vat/apply-economic-operator-registration-identification-number")
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
        redirectLocation(result) mustBe Some("/register-for-vat/estimate-vat-taxable-turnover-next-12-months")
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

  "applyEoriPage" should {
    "return an Ok when there is a trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails(applyEori = Some(true))))

      callAuthorised(testController.applyEoriPage) {
        result => {
          status(result) mustBe OK
        }
      }
    }

    "return an Ok when there is no trading details present" in new Setup {
      when(mockTradingDetailsService.getTradingDetailsViewModel(any())(any(), any()))
        .thenReturn(Future.successful(TradingDetails()))

      callAuthorised(testController.applyEoriPage) {
        result => {
          status(result) mustBe OK
        }
      }
    }
  }

  "submitApplyEori" should {

    val fakeRequest = FakeRequest(controllers.routes.TradingDetailsController.submitApplyEori())

    "return 303 when they want to apply for eori number" in new Setup {
      when(mockTradingDetailsService.saveEori(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> "true"
      )

      submitAuthorised(testController.submitApplyEori, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/estimate-vat-taxable-turnover-next-12-months")
      }
    }

    "return 303 when they don't want to apply for an eori number" in new Setup {
      when(mockTradingDetailsService.saveEori(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "applyEoriRadio" -> "false"
      )

      submitAuthorised(testController.submitApplyEori, request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/estimate-vat-taxable-turnover-next-12-months")
      }
    }

    "return 400 when no option is selected" in new Setup {
      when(mockTradingDetailsService.saveEori(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullS4L))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(testController.submitApplyEori, request) { result =>
        status(result) mustBe 400
      }
    }

  }
}
