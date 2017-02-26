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

package services

import connectors.{KeystoreConnector, VatRegistrationConnector}
import enums.{CacheKeys, DownstreamOutcome}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatChoice, VatScheme}
import models.view._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads}

import scala.concurrent.Future

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()
  val mockRegConnector = mock[VatRegistrationConnector]

  class Setup {
    val service = new VatRegistrationService(mockS4LService, mockRegConnector) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.createNewRegistration()(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      ScalaFutures.whenReady(service.assertRegistrationFootprint())(_ mustBe DownstreamOutcome.Success)
    }
  }

  "Calling submitVatScheme" should {
    "return a downstream success when both trading details and vat-choice are upserted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.StartDate.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[StartDate]]()))
        .thenReturn(Future.successful(Some(validStartDate)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.VoluntaryRegistration.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[VoluntaryRegistration]]()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))))

      when(mockRegConnector.upsertVatChoice(Matchers.any(), Matchers.any())
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatChoice]]()))
        .thenReturn(Future.successful(validVatChoice))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.TradingName.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[TradingName]]()))
        .thenReturn(Future.successful(Some(validTradingName)))

      when(mockRegConnector.upsertVatTradingDetails(Matchers.any(), Matchers.any())
      (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.EstimateVatTurnover.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[EstimateVatTurnover]]()))
        .thenReturn(Future.successful(Some(validEstimateVatTurnover)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.EstimateZeroRatedSales.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[EstimateZeroRatedSales]]()))
        .thenReturn(Future.successful(Some(validEstimateZeroRatedSales)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.VatChargeExpectancy.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[VatChargeExpectancy]]()))
        .thenReturn(Future.successful(Some(validVatChargeExpectancy)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.VatReturnFrequency.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[VatReturnFrequency]]()))
        .thenReturn(Future.successful(Some(validVatReturnFrequency)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.AccountingPeriod.toString))
      (Matchers.any[HeaderCarrier](), Matchers.any[Format[AccountingPeriod]]()))
        .thenReturn(Future.successful(Some(validAccountingPeriod)))

      when(mockRegConnector.upsertVatFinancials(Matchers.any(), Matchers.any())
      (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatFinancials))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      ScalaFutures.whenReady(service.submitVatScheme())(_ mustBe DownstreamOutcome.Success)
    }
  }

  "Calling submitVatChoice" should {
    "return a success response when a VatChoice is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.StartDate.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[StartDate]]()))
          .thenReturn(Future.successful(Some(validStartDate)))

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.VoluntaryRegistration.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[VoluntaryRegistration]]()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatChoice(Matchers.any(), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatChoice]]()))
        .thenReturn(Future.successful(validVatChoice))

      ScalaFutures.whenReady(service.submitVatChoice())(_ mustBe validVatChoice)
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet(Matchers.eq(CacheKeys.TradingName.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[TradingName]]()))
        .thenReturn(Future.successful(Some(validTradingName)))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatTradingDetails(Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      ScalaFutures.whenReady(service.submitTradingDetails())(_ mustBe validVatTradingDetails)
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successfully" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))
      ScalaFutures.whenReady(service.deleteVatScheme())(_ mustBe true)
    }
  }
}
