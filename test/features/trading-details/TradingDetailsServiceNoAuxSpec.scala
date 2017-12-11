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

package features.trading

import connectors.RegistrationConnector
import features.tradingDetails.models._
import features.tradingDetails.services.TradingDetailsSrv
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import services.S4LService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class TradingDetailsServiceNoAuxSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new TradingDetailsSrv {
      override val s4lService: S4LService = mockS4LService
      override val registrationConnector : RegistrationConnector = mockRegConnector
    }
  }

  val regId = "regID"
  val tradingName = Some("testTradingName")

  val tradingNameViewNo = TradingNameView(TradingNameView.TRADING_NAME_NO, None)
  val tradingNameViewYes = TradingNameView(TradingNameView.TRADING_NAME_YES, tradingName)

  val euGoodsViewYes = EuGoodsView(EuGoodsView.EU_GOODS_YES)

  val applyEoriViewNo = ApplyEoriView(ApplyEoriView.APPLY_EORI_NO)

  val fullTradingDetails = TradingDetails(None, Some(false))

  val noNameNoEuGoodsTradingDetails = Some(TradingDetails(None, None))
  val hasNameNoEuGoodsTradingDetails = Some(TradingDetails(tradingName, None))

  val noNameYesEoriTradingDetails = Some(TradingDetails(None, Some(true)))
  val noNameNoEoriTradingDetails = Some(fullTradingDetails)

  val emptyS4L = S4LTradingDetails()
  val incompleteS4L = S4LTradingDetails(
    Some(tradingNameViewYes)
  )
  val twoPageS4L = S4LTradingDetails(
    Some(tradingNameViewNo),
    Some(euGoodsViewYes),
    None
  )
  val fullS4L = S4LTradingDetails(
    Some(tradingNameViewNo),
    Some(euGoodsViewYes),
    Some(applyEoriViewNo)
  )

  val twoPageS4LNo = S4LTradingDetails(
    Some(tradingNameViewNo),
    Some(EuGoodsView(EuGoodsView.EU_GOODS_NO)),
    None
  )

  "convertRegistrationModel" should {
    "return an empty S4L data model if nothing was saved to the backend" in new Setup() {
      service.convertRegistrationModel(None) mustBe S4LTradingDetails()
    }

    "return a S4L data model without a trading name and no eu goods" in new Setup() {
      service.convertRegistrationModel(noNameNoEuGoodsTradingDetails) mustBe S4LTradingDetails(
        Some(TradingNameView(TradingNameView.TRADING_NAME_NO, None)),
        Some(EuGoodsView(EuGoodsView.EU_GOODS_NO)),
        None
      )
    }

    "return a S4L data model with a trading name and no eu goods" in new Setup() {
      service.convertRegistrationModel(hasNameNoEuGoodsTradingDetails) mustBe S4LTradingDetails(
        Some(TradingNameView(TradingNameView.TRADING_NAME_YES, tradingName)),
        Some(EuGoodsView(EuGoodsView.EU_GOODS_NO)),
        None
      )
    }

    "return a S4L data model without a trading name and applying for an EORI" in new Setup() {
      service.convertRegistrationModel(noNameYesEoriTradingDetails) mustBe S4LTradingDetails(
        Some(TradingNameView(TradingNameView.TRADING_NAME_NO, None)),
        Some(EuGoodsView(EuGoodsView.EU_GOODS_YES)),
        Some(ApplyEoriView(ApplyEoriView.APPLY_EORI_YES))
      )
    }

    "return a S4L data model without a trading name and not applying for an EORI" in new Setup() {
      service.convertRegistrationModel(noNameNoEoriTradingDetails) mustBe S4LTradingDetails(
        Some(TradingNameView(TradingNameView.TRADING_NAME_NO, None)),
        Some(EuGoodsView(EuGoodsView.EU_GOODS_YES)),
        Some(ApplyEoriView(ApplyEoriView.APPLY_EORI_NO))
      )
    }
  }

  "getTradingDetailsViewModel" should {
    "return the S4L model if it is there" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyS4L)))

      await(service.getTradingDetailsViewModel(regId)) mustBe emptyS4L
    }

    "return the converted backend model if the S4L is not there" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockRegConnector.getTradingDetails(any())(any(), any()))
        .thenReturn(Future.successful(hasNameNoEuGoodsTradingDetails))

      await(service.getTradingDetailsViewModel(regId)) mustBe S4LTradingDetails(
        Some(TradingNameView(TradingNameView.TRADING_NAME_YES, tradingName)),
        Some(EuGoodsView(EuGoodsView.EU_GOODS_NO)),
        None
      )
    }

    "return an empty backend model if the S4L is not there and neither is the backend" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockRegConnector.getTradingDetails(any())(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.getTradingDetailsViewModel(regId)) mustBe emptyS4L
    }
  }

  "completedTradingDetails" should {
    "convert a S4L model to the backend where all 3 pages were answered" in new Setup() {
      service.completedTradingDetails(fullS4L) mustBe Right(TradingDetails(None, Some(false)))
    }

    "convert a S4L model to the backend where there are no eu goods" in new Setup() {
      service.completedTradingDetails(twoPageS4LNo) mustBe Right(TradingDetails(None, None))
    }

    "return back the S4L model if it is incomplete" in new Setup() {
      service.completedTradingDetails(emptyS4L) mustBe Left(emptyS4L)
    }
  }

  "submitTradingDetails" should {
    "if the S4L model is incomplete, save to S4L" in new Setup() {
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.submitTradingDetails(regId, incompleteS4L)) mustBe true
    }

    "if the S4L model is complete, save to the backend and clear S4L" in new Setup() {
      when(mockRegConnector.upsertTradingDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullTradingDetails))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.submitTradingDetails(regId, fullS4L)) mustBe true
    }
  }

  "saveTradingName" should {
    "amend the trading name on a S4L model" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4L)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveTradingName(regId, tradingNameViewNo)) mustBe true
    }
  }

  "saveEuGoods" should {
    "amend the eu goods on a S4L model" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4L)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveEuGoods(regId, euGoodsViewYes)) mustBe true
    }
  }

  "saveEori" should {
    "amend the trading name on a S4L model" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[S4LTradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(twoPageS4L)))
      when(mockRegConnector.upsertTradingDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(fullTradingDetails))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.saveEori(regId, applyEoriViewNo)) mustBe true
    }
  }
}
