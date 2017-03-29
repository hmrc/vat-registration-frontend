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
import enums.DownstreamOutcome
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.api._
import models.view.sicAndCompliance.{BusinessActivityDescription, CulturalComplianceQ1}
import models.view.vatFinancials._
import models.view.vatTradingDetails.{StartDateView, TradingNameView, VoluntaryRegistration}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads}

import scala.concurrent.Future

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with ScalaFutures {

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

      whenReady(service.assertRegistrationFootprint())(_ mustBe DownstreamOutcome.Success)
    }
  }

  "Calling submitVatScheme" should {
    "return a downstream success when both trading details and vat-choice are upserted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[StartDateView]()(Matchers.eq(S4LKey[StartDateView]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(StartDateView(StartDateView.SPECIFIC_DATE, someTestDate))))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(Matchers.eq(S4LKey[VoluntaryRegistration]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))))

      when(mockRegConnector.upsertVatChoice(Matchers.any(), Matchers.any())
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatChoice]]()))
        .thenReturn(Future.successful(validVatChoice))

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(TradingNameView(yesNo = TradingNameView.TRADING_NAME_NO))))

      when(mockRegConnector.upsertVatTradingDetails(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      when(mockS4LService.fetchAndGet[EstimateVatTurnover]()(Matchers.eq(S4LKey[EstimateVatTurnover]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validEstimateVatTurnover)))

      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()(Matchers.eq(S4LKey[EstimateZeroRatedSales]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validEstimateZeroRatedSales)))

      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()(Matchers.eq(S4LKey[VatChargeExpectancy]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validVatChargeExpectancy)))

      when(mockS4LService.fetchAndGet[VatReturnFrequency]()(Matchers.eq(S4LKey[VatReturnFrequency]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validVatReturnFrequency)))

      when(mockS4LService.fetchAndGet[AccountingPeriod]()(Matchers.eq(S4LKey[AccountingPeriod]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validAccountingPeriod)))

      when(mockS4LService.fetchAndGet[CompanyBankAccountDetails]()(Matchers.eq(S4LKey[CompanyBankAccountDetails]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validBankAccountDetails)))

      when(mockRegConnector.upsertVatFinancials(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatFinancials))

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validBusinessActivityDescription)))

      when(mockS4LService.fetchAndGet[CulturalComplianceQ1]()(Matchers.eq(S4LKey[CulturalComplianceQ1]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validCulturalComplianceQ1)))

      when(mockRegConnector.upsertSicAndCompliance(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      whenReady(service.submitVatScheme())(_ mustBe DownstreamOutcome.Success)
    }
  }

  "Calling submitVatChoice" should {
    "return a success response when a VatChoice is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[StartDateView]()(Matchers.eq(S4LKey[StartDateView]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(StartDateView(dateType = StartDateView.SPECIFIC_DATE, date = someTestDate))))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(Matchers.eq(S4LKey[VoluntaryRegistration]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatChoice(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatChoice))

      //      whenReady(service.submitVatChoice())(_ mustBe validVatChoice)
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(TradingNameView(yesNo = TradingNameView.TRADING_NAME_YES))))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatTradingDetails(Matchers.any(), Matchers.any())
      (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      whenReady(service.submitTradingDetails())(_ mustBe validVatTradingDetails)
    }

    "return a success response when VatTradingDetails is submitted and no Trading Name is found in S4L" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatTradingDetails(regId = Matchers.any(), vatTradingDetails = Matchers.any())
      (hc = Matchers.any(), rds = Matchers.any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      whenReady(service.submitTradingDetails())(_ mustBe validVatTradingDetails)
    }
  }

  "Calling submitSicAndCompliance" should {
    "return a success response when SicAndCompliance is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validBusinessActivityDescription)))

      when(mockS4LService.fetchAndGet[CulturalComplianceQ1]()(Matchers.eq(S4LKey[CulturalComplianceQ1]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(validCulturalComplianceQ1)))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertSicAndCompliance(Matchers.any(), Matchers.any())
      (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      whenReady(service.submitSicAndCompliance())(_ mustBe validSicAndCompliance)
    }

    "return a success response when SicAndCompliance is submitted and no Business Activity Description is found in S4L" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertSicAndCompliance(regId = Matchers.any(), sicAndCompliance = Matchers.any())
      (hc = Matchers.any(), rds = Matchers.any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      whenReady(service.submitSicAndCompliance())(_ mustBe validSicAndCompliance)
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))
      whenReady(service.deleteVatScheme())(_ mustBe true)
    }
  }

  "Calling deleteAccountingPeriodStart" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteAccountingPeriodStart(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))
      whenReady(service.deleteAccountingPeriodStart())(_ mustBe true)
    }
  }

  "Calling deleteBankAccountDetails" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteBankAccount(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))
      whenReady(service.deleteBankAccountDetails())(_ mustBe true)
    }
  }

  "Calling deleteZeroRatedTurnover" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteZeroRatedTurnover(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(true))
      whenReady(service.deleteZeroRatedTurnover())(_ mustBe true)
    }
  }

  "When this is the first time the user starts a journey and we're persisting to the backend" should {

    "submitVatFinancials should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatFinancials = VatFinancials(
        bankAccount = Some(validBankAccount),
        turnoverEstimate = 50000,
        zeroRatedTurnoverEstimate = Some(60000),
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(emptyVatScheme))
      whenReady(service.submitVatFinancials())(_ mustBe mergedVatFinancials)
    }

    "submitTradingDetails should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatTradingDetails = validVatTradingDetails
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(emptyVatScheme))
      whenReady(service.submitTradingDetails())(_ mustBe mergedVatTradingDetails)
    }

    "submitVatChoice should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      //      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(Matchers.any[HeaderCarrier](), Matchers.any[HttpReads[VatScheme]]()))
      //        .thenReturn(Future.successful(emptyVatScheme))
      //      whenReady(service.submitVatChoice())(_ mustBe validVatChoice)
    }

  }
}
