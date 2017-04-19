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
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.vatContact.BusinessContactDetails
import models.view.vatFinancials._
import models.view.vatFinancials.vatAccountingPeriod.{AccountingPeriod, VatReturnFrequency}
import models.view.vatFinancials.vatBankAccount.CompanyBankAccountDetails
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import models.{S4LKey, VatBankAccountPath}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads}

import scala.concurrent.Future
import scala.language.postfixOps

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
      when(mockRegConnector.createNewRegistration()(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      service.createRegistrationFootprint() completedSuccessfully
    }
  }

  "Calling submitVatScheme" should {
    "return a downstream success when both trading details and vat-choice are upserted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[StartDateView]()(Matchers.eq(S4LKey[StartDateView]), any(), any()))
        .thenReturn(Future.successful(Some(StartDateView(StartDateView.SPECIFIC_DATE, someTestDate))))

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(Matchers.eq(S4LKey[VoluntaryRegistration]), any(), any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))))

      when(mockS4LService.fetchAndGet[VoluntaryRegistrationReason]()(Matchers.eq(S4LKey[VoluntaryRegistrationReason]), any(), any()))
        .thenReturn(Future.successful(Some(VoluntaryRegistrationReason(VoluntaryRegistrationReason.SELLS))))

      when(mockRegConnector.upsertVatChoice(any(), any())
      (any[HeaderCarrier](), any[HttpReads[VatChoice]]()))
        .thenReturn(Future.successful(validVatChoice))

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), any(), any()))
        .thenReturn(Future.successful(Some(TradingNameView(yesNo = TradingNameView.TRADING_NAME_NO))))

      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      when(mockS4LService.fetchAndGet[EstimateVatTurnover]()(Matchers.eq(S4LKey[EstimateVatTurnover]), any(), any()))
        .thenReturn(Future.successful(Some(validEstimateVatTurnover)))

      when(mockS4LService.fetchAndGet[EstimateZeroRatedSales]()(Matchers.eq(S4LKey[EstimateZeroRatedSales]), any(), any()))
        .thenReturn(Future.successful(Some(validEstimateZeroRatedSales)))

      when(mockS4LService.fetchAndGet[VatChargeExpectancy]()(Matchers.eq(S4LKey[VatChargeExpectancy]), any(), any()))
        .thenReturn(Future.successful(Some(validVatChargeExpectancy)))

      when(mockS4LService.fetchAndGet[VatReturnFrequency]()(Matchers.eq(S4LKey[VatReturnFrequency]), any(), any()))
        .thenReturn(Future.successful(Some(validVatReturnFrequency)))

      when(mockS4LService.fetchAndGet[AccountingPeriod]()(Matchers.eq(S4LKey[AccountingPeriod]), any(), any()))
        .thenReturn(Future.successful(Some(validAccountingPeriod)))

      when(mockS4LService.fetchAndGet[CompanyBankAccountDetails]()(Matchers.eq(S4LKey[CompanyBankAccountDetails]), any(), any()))
        .thenReturn(Future.successful(Some(validBankAccountDetails)))

      when(mockRegConnector.upsertVatFinancials(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatFinancials))

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), any(), any()))
        .thenReturn(Future.successful(Some(validBusinessActivityDescription)))

      when(mockS4LService.fetchAndGet[NotForProfit]()(Matchers.eq(S4LKey[NotForProfit]), any(), any()))
        .thenReturn(Future.successful(Some(validNotForProfit)))

      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(Matchers.eq(S4LKey[CompanyProvideWorkers]), any(), any()))
        .thenReturn(Future.successful(Some(validCompanyProvideWorkers)))

      when(mockS4LService.fetchAndGet[Workers]()(Matchers.eq(S4LKey[Workers]), any(), any()))
        .thenReturn(Future.successful(Some(validWorkers)))

      when(mockS4LService.fetchAndGet[TemporaryContracts]()(Matchers.eq(S4LKey[TemporaryContracts]), any(), any()))
        .thenReturn(Future.successful(Some(validTemporaryContracts)))

      when(mockS4LService.fetchAndGet[SkilledWorkers]()(Matchers.eq(S4LKey[SkilledWorkers]), any(), any()))
        .thenReturn(Future.successful(Some(validSkilledWorkers)))

      when(mockS4LService.fetchAndGet[AdviceOrConsultancy]()(Matchers.eq(S4LKey[AdviceOrConsultancy]), any(), any()))
        .thenReturn(Future.successful(Some(validAdviceOrConsultancy)))

      when(mockS4LService.fetchAndGet[ActAsIntermediary]()(Matchers.eq(S4LKey[ActAsIntermediary]), any(), any()))
        .thenReturn(Future.successful(Some(validActAsIntermediary)))

      when(mockS4LService.fetchAndGet[ChargeFees]()(Matchers.eq(S4LKey[ChargeFees]), any(), any()))
        .thenReturn(Future.successful(Some(ChargeFees(true))))

      when(mockS4LService.fetchAndGet[LeaseVehicles]()(Matchers.eq(S4LKey[LeaseVehicles]), any(), any()))
        .thenReturn(Future.successful(Some(LeaseVehicles(true))))

      when(mockS4LService.fetchAndGet[AdditionalNonSecuritiesWork]()(Matchers.eq(S4LKey[AdditionalNonSecuritiesWork]), any(), any()))
        .thenReturn(Future.successful(Some(AdditionalNonSecuritiesWork(true))))

      when(mockS4LService.fetchAndGet[EuGoods]()(Matchers.eq(S4LKey[EuGoods]), any(), any()))
        .thenReturn(Future.successful(Some(validEuGoods)))

      when(mockS4LService.fetchAndGet[ApplyEori]()(Matchers.eq(S4LKey[ApplyEori]), any(), any()))
        .thenReturn(Future.successful(Some(validApplyEori)))

      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      when(mockS4LService.fetchAndGet[BusinessContactDetails]()(Matchers.eq(S4LKey[BusinessContactDetails]), any(), any()))
        .thenReturn(Future.successful(Some(validBusinessContactDetails)))

      when(mockRegConnector.upsertVatContact(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatContact))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      service.submitVatScheme() completedSuccessfully
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), any(), any()))
        .thenReturn(Future.successful(Some(TradingNameView(yesNo = TradingNameView.TRADING_NAME_YES))))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      service.submitTradingDetails() returns validVatTradingDetails
    }

    "return a success response when VatTradingDetails is submitted and no Trading Name is found in S4L" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[TradingNameView]()(Matchers.eq(S4LKey[TradingNameView]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatTradingDetails(regId = any(), vatTradingDetails = any())(hc = any(), rds = any()))
        .thenReturn(Future.successful(validVatTradingDetails))

      when(mockS4LService.fetchAndGet[EuGoods]()(Matchers.eq(S4LKey[EuGoods]), any(), any()))
        .thenReturn(Future.successful(Some(validEuGoods)))

      when(mockS4LService.fetchAndGet[ApplyEori]()(Matchers.eq(S4LKey[ApplyEori]), any(), any()))
        .thenReturn(Future.successful(Some(validApplyEori)))

      service.submitTradingDetails() returns validVatTradingDetails
    }
  }

  "Calling submitSicAndCompliance" should {
    "return a success response when SicAndCompliance is submitted" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), any(), any()))
        .thenReturn(Future.successful(Some(validBusinessActivityDescription)))

      when(mockS4LService.fetchAndGet[NotForProfit]()(Matchers.eq(S4LKey[NotForProfit]), any(), any()))
        .thenReturn(Future.successful(Some(validNotForProfit)))

      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(Matchers.eq(S4LKey[CompanyProvideWorkers]), any(), any()))
        .thenReturn(Future.successful(Some(validCompanyProvideWorkers)))

      when(mockS4LService.fetchAndGet[Workers]()(Matchers.eq(S4LKey[Workers]), any(), any()))
        .thenReturn(Future.successful(Some(validWorkers)))

      when(mockS4LService.fetchAndGet[TemporaryContracts]()(Matchers.eq(S4LKey[TemporaryContracts]), any(), any()))
        .thenReturn(Future.successful(Some(validTemporaryContracts)))

      when(mockS4LService.fetchAndGet[AdviceOrConsultancy]()(Matchers.eq(S4LKey[AdviceOrConsultancy]), any(), any()))
        .thenReturn(Future.successful(Some(validAdviceOrConsultancy)))

      when(mockS4LService.fetchAndGet[ActAsIntermediary]()(Matchers.eq(S4LKey[ActAsIntermediary]), any(), any()))
        .thenReturn(Future.successful(Some(validActAsIntermediary)))

      when(mockS4LService.fetchAndGet[ChargeFees]()(Matchers.eq(S4LKey[ChargeFees]), any(), any()))
        .thenReturn(Future.successful(Some(ChargeFees(true))))

      when(mockS4LService.fetchAndGet[AdditionalNonSecuritiesWork]()(Matchers.eq(S4LKey[AdditionalNonSecuritiesWork]), any(), any()))
        .thenReturn(Future.successful(Some(AdditionalNonSecuritiesWork(true))))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertSicAndCompliance(any(), any())
      (any(), any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      service.submitSicAndCompliance() returns validSicAndCompliance
    }

    "return a success response when SicAndCompliance is submitted and no Business Activity Description is found in S4L" in new Setup {
      mockFetchRegId(validRegId)

      when(mockS4LService.fetchAndGet[BusinessActivityDescription]()(Matchers.eq(S4LKey[BusinessActivityDescription]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockS4LService.fetchAndGet[CompanyProvideWorkers]()(Matchers.eq(S4LKey[CompanyProvideWorkers]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))
      (any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockS4LService.fetchAndGet[SkilledWorkers]()(Matchers.eq(S4LKey[SkilledWorkers]), any(), any()))
        .thenReturn(Future.successful(Some(validSkilledWorkers)))

      when(mockRegConnector.upsertSicAndCompliance(regId = any(), sicAndCompliance = any())
      (hc = any(), rds = any()))
        .thenReturn(Future.successful(validSicAndCompliance))

      service.submitSicAndCompliance() returns validSicAndCompliance
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(any())(any(), any()))
        .thenReturn(Future.successful(true))
      service.deleteVatScheme() returns true
    }
  }

  "Calling deleteElement" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any()))
        .thenReturn(Future.successful(true))
      service.deleteElement(VatBankAccountPath) returns true
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

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(emptyVatScheme))
      service.submitVatFinancials() returns mergedVatFinancials
    }

    "submitTradingDetails should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatTradingDetails = validVatTradingDetails
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(emptyVatScheme))
      service.submitTradingDetails() returns mergedVatTradingDetails
    }


    "submitVatContact should process the submission even if VatScheme does not contain a VatContact object" in new Setup {
      val mergedvalidVatContact = validVatContact
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any[HeaderCarrier](), any[HttpReads[VatScheme]]()))
        .thenReturn(Future.successful(emptyVatScheme))
      service.submitVatContact() returns mergedvalidVatContact
    }

  }
}
