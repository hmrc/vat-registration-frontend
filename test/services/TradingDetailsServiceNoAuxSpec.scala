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

package services

import fixtures.VatRegistrationFixture
import models.{TradingNameView, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import testHelpers.{S4LMockSugar, VatRegSpec}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class TradingDetailsServiceNoAuxSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service: TradingDetailsService = new TradingDetailsService(
      mockS4LService,
      mockVatRegistrationConnector,
      mockPrePopulationService
    )
  }

  val regId = "regID"
  val tradingName = Some("testTradingName")

  val tradingNameViewNo = TradingNameView(yesNo = false, None)
  val tradingNameViewYes = TradingNameView(yesNo = true, tradingName)

  val emptyS4L = TradingDetails()
  val incompleteS4L = TradingDetails(
    Some(tradingNameViewYes)
  )

  val fullS4L = TradingDetails(
    Some(tradingNameViewNo),
    Some(true)
  )

  val fullS4LWithTradingName = TradingDetails(
    Some(tradingNameViewYes),
    Some(true)
  )

  val twoPageS4LNo = TradingDetails(
    Some(tradingNameViewNo),
    Some(false)
  )

  "getTradingDetailsViewModel" should {
    "return the S4L model if it is there" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[TradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyS4L)))

      await(service.getTradingDetailsViewModel(regId)) mustBe emptyS4L
    }

    "return the converted backend model if the S4L is not there" in new Setup() {
      val tradingNameNoEu = TradingDetails(
        Some(TradingNameView(yesNo = true, tradingName)),
        Some(false)
      )

      when(mockS4LService.fetchAndGetNoAux[TradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getTradingDetails(any())(any()))
        .thenReturn(Future.successful(Some(tradingNameNoEu)))

      await(service.getTradingDetailsViewModel(regId)) mustBe tradingNameNoEu
    }

    "return an empty backend model if the S4L is not there and neither is the backend" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[TradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getTradingDetails(any())(any()))
        .thenReturn(Future.successful(None))

      await(service.getTradingDetailsViewModel(regId)) mustBe emptyS4L
    }
  }

  "getS4LCompletion" should {
    "convert a S4L model to the backend where all 3 pages were answered" in new Setup() {
      service.getS4LCompletion(fullS4L) mustBe Complete(fullS4L)
    }

    "convert a S4L model to the backend where there are no eu goods" in new Setup() {
      service.getS4LCompletion(twoPageS4LNo) mustBe Complete(twoPageS4LNo)
    }

    "return back the S4L model if it is incomplete" in new Setup() {
      service.getS4LCompletion(emptyS4L) mustBe Incomplete(emptyS4L)
    }
  }

  "submitTradingDetails" should {
    "if the S4L model is incomplete, save to S4L" in new Setup() {
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.submitTradingDetails(regId, incompleteS4L)) mustBe incompleteS4L
    }

    "if the S4L model is complete, save to the backend and clear S4L" in new Setup() {
      when(mockVatRegistrationConnector.upsertTradingDetails(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.submitTradingDetails(regId, fullS4L)) mustBe fullS4L
    }

  }

  "saveTradingName" should {
    "amend the trading name on a S4L model should not save to pre pop" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[TradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4L)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveTradingName(regId, tradingNameViewNo.yesNo, tradingNameViewNo.tradingName)) mustBe TradingDetails(Some(tradingNameViewNo))
    }
  }

  "saveEuGoods" should {
    "save a complete model to the backend and clear S4L" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[TradingDetails](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4L)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockVatRegistrationConnector.upsertTradingDetails(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveEuGoods(regId, euGoods = true)) mustBe incompleteS4L.copy(euGoods = Some(true))
    }
  }
}
