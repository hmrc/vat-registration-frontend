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

import java.time.LocalDate

import cats.data.OptionT
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models._
import models.api._
import models.external.CoHoCompanyProfile
import models.view.frs._
import models.view.ppob.PpobView
import models.view.vatFinancials.{EstimateVatTurnover, ZeroRatedSales}
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new VatRegistrationService(mockS4LService, mockRegConnector, mockCompanyRegConnector) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(validRegId)
  }

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.createNewRegistration()(any(), any())).thenReturn(validVatScheme.pure)

      mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))
      when(mockCompanyRegConnector.getCompanyRegistrationDetails(any())(any())).thenReturn(OptionT.some(validCoHoProfile))

      service.createRegistrationFootprint() completedSuccessfully
    }
  }

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created without finding a company profile" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.createNewRegistration()(any(), any())).thenReturn(validVatScheme.pure)
      mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))
      when(mockCompanyRegConnector.getCompanyRegistrationDetails(any())(any())).thenReturn(OptionT.none[Future, CoHoCompanyProfile])

      service.createRegistrationFootprint() completedSuccessfully
    }
  }

  "Calling getAckRef" should {
    "retrieve Acknowledgement Reference (id) from the backend" in new Setup {
      when(mockRegConnector.getAckRef(Matchers.eq(validRegId))(any())).thenReturn(OptionT.some("testRefNo"))
      service.getAckRef(validRegId) returnsSome "testRefNo"
    }
    "retrieve no Acknowledgement Reference if there's none in the backend" in new Setup {
      when(mockRegConnector.getAckRef(Matchers.eq(validRegId))(any())).thenReturn(OptionT.none[Future, String])
      service.getAckRef(validRegId) returnsNone
    }
  }

  "Calling getFlatRateSchemeThreshold" should {
    "return 0 if no EstimateVatTurnover can be found anywhere" in new Setup {
      when(mockS4LService.getViewModel[EstimateVatTurnover, S4LVatFinancials]()(any(), any(), any(), any())).thenReturn(OptionT.none[Future, EstimateVatTurnover])
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)

      service.getFlatRateSchemeThreshold() returns 0L
    }


    "return 1000 if EstimateVatTurnover in the backend is 50'000" in new Setup {
      when(mockS4LService.getViewModel[EstimateVatTurnover, S4LVatFinancials]()(any(), any(), any(), any())).thenReturn(OptionT.none[Future, EstimateVatTurnover])
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)

      service.getFlatRateSchemeThreshold() returns 1000L
    }


    "return correct number (2% rounded to nearest pound if EstimateVatTurnover is in Save 4 Later" in new Setup {
      forAll(Seq[(Int, Double)](
        1000 -> 20,
        100 -> 2,
        49 -> 1,
        12324 -> 246, // 246.48 rounded down
        12325 -> 247 // 246.5 rounded up
      )) {
        case (estimate, expectedFlatRateThreshold) =>
          when(mockS4LService.getViewModel[EstimateVatTurnover, S4LVatFinancials]()(any(), any(), any(), any()))
            .thenReturn(OptionT.some(EstimateVatTurnover(estimate)))
          service.getFlatRateSchemeThreshold() returns expectedFlatRateThreshold
      }
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      save4laterReturns(S4LTradingDetails(tradingName = Some(validTradingNameView)))
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }

    "return a success response when start date choice is BUSINESS_START_DATE" in new Setup {
      val tradingDetailsWithCtActiveDateSelected = tradingDetails(startDateSelection = StartDateView.BUSINESS_START_DATE)

      save4laterReturns(S4LTradingDetails(
        startDate = Some(StartDateView(dateType = StartDateView.BUSINESS_START_DATE, ctActiveDate = someTestDate))
      ))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(tradingDetailsWithCtActiveDateSelected.pure)

      service.submitTradingDetails() returns tradingDetailsWithCtActiveDateSelected
    }

    "return a success response when VatTradingDetails is submitted and no Trading Name is found in S4L" in new Setup {
      save4laterReturnsNothing[S4LTradingDetails]()
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }
  }

  "Calling submitSicAndCompliance" should {
    "return a success response when SicAndCompliance is submitted" in new Setup {
      save4laterReturnsNothing[S4LVatSicAndCompliance]()
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }

    "return a success response when SicAndCompliance is submitted for the first time" in new Setup {
      save4laterReturns(S4LVatSicAndCompliance(skilledWorkers = Some(validSkilledWorkers)))
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.copy(vatSicAndCompliance = None).pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }
  }

  "Calling submitEligibility" should {
    "return a success response when VatEligibility is submitted" in new Setup {
      save4laterReturns(S4LVatEligibility(Some(validServiceEligibility)))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)

      service.submitVatEligibility() returns validServiceEligibility
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(any())(any(), any())).thenReturn(().pure)

      service.deleteVatScheme() completedSuccessfully
    }
  }

  "Calling deleteElement" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElement(VatBankAccountPath) completedSuccessfully
    }
  }

  "Calling conditionalDeleteElement" should {

    "deleteElement when cond is true" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.conditionalDeleteElement(VatBankAccountPath, true) completedSuccessfully

      verify(mockRegConnector, times(1)).deleteElement(any())(any())(any(), any())
    }

    "not deleteElement when cond is false" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      service.conditionalDeleteElement(VatBankAccountPath, false) completedSuccessfully

      verify(mockRegConnector, times(0)).deleteElement(any())(any())(any(), any())
    }
  }

  "Calling deleteElements with items" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElements(List(VatBankAccountPath, ZeroRatedTurnoverEstimatePath)) completedSuccessfully
    }
  }

  "Calling deleteElements without items" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElements(List()) completedSuccessfully
    }
  }

  "When this is the first time the user starts a journey and we're persisting to the backend" should {

    "submitVatFinancials should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatFinancials = VatFinancials(
        bankAccount = Some(validBankAccount),
        turnoverEstimate = validEstimateVatTurnover.vatTurnoverEstimate,
        zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales.zeroRatedTurnoverEstimate),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod("monthly")
      )

      save4laterReturns(S4LVatFinancials(
        estimateVatTurnover = Some(validEstimateVatTurnover),
        zeroRatedTurnover = Some(ZeroRatedSales.yes),
        zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales),
        vatChargeExpectancy = Some(validVatChargeExpectancy),
        vatReturnFrequency = Some(validVatReturnFrequency),
        accountingPeriod = Some(validAccountingPeriod),
        companyBankAccount = Some(validCompanyBankAccount),
        companyBankAccountDetails = Some(validBankAccountDetails)))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatFinancials(any(), any())(any(), any())).thenReturn(validVatFinancials.pure)

      service.submitVatFinancials() returns mergedVatFinancials
    }

    "submitVatFinancials should fail if there's not trace of VatFinancials in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatFinancials]()

      service.submitVatFinancials() failedWith classOf[IllegalStateException]
    }

    "submitTradingDetails should fail if there's not trace of VatTradingDetails in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LTradingDetails]()

      service.submitTradingDetails() failedWith classOf[IllegalStateException]
    }

    "submitVatContact should process the submission even if VatScheme does not contain a VatContact object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatContact(any(), any())(any(), any())).thenReturn(validVatContact.pure)
      save4laterReturns(S4LVatContact(businessContactDetails = Some(validBusinessContactDetails)))
      service.submitVatContact() returns validVatContact
    }

    "submitVatContact should fail if there's not trace of VatContact in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatContact]()

      service.submitVatContact() failedWith classOf[IllegalStateException]
    }

    "submitVatEligibility should process the submission even if VatScheme does not contain a VatEligibility object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)
      save4laterReturns(S4LVatEligibility(Some(validServiceEligibility)))
      service.submitVatEligibility() returns validServiceEligibility
    }

    "submitVatEligibility should fail if there's not trace of VatEligibility in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatEligibility]()

      service.submitVatEligibility() failedWith classOf[IllegalStateException]
    }

    "submitVatLodgingOfficer should process the submission even if VatScheme does not contain a VatLodgingOfficer object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatLodgingOfficer(any(), any())(any(), any())).thenReturn(validLodgingOfficer.pure)
      save4laterReturns(S4LVatLodgingOfficer(
        officerHomeAddress = Some(OfficerHomeAddressView("")),
        officerSecurityQuestions = Some(OfficerSecurityQuestionsView(LocalDate.now, NINO)),
        completionCapacity = Some(CompletionCapacityView(""))
      ))
      service.submitVatLodgingOfficer() returns validLodgingOfficer
    }

    "submitVatLodgingOfficer should fail if there's not trace of VatLodgingOfficer in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatLodgingOfficer]()

      service.submitVatLodgingOfficer() failedWith classOf[IllegalStateException]
    }

    "submitVatFlatRateScheme should process the submission even if VatScheme does not contain VatFlatRateScheme" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      save4laterReturns(S4LFlatRateScheme(
        joinFrs = Some(JoinFrsView(true)),
        annualCostsInclusive = Some(AnnualCostsInclusiveView("yes")),
        annualCostsLimited = Some(AnnualCostsLimitedView("yes")),
        registerForFrs = Some(RegisterForFrsView(true)),
        frsStartDate = Some(FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE))
      ))
      service.submitVatFlatRateScheme() returns validVatFlatRateScheme
    }

    "submitVatFlatRateScheme should fail if there's no VatFlatRateScheme in backend or S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LFlatRateScheme]()

      service.submitVatFlatRateScheme() failedWith classOf[IllegalStateException]
    }

    "submitPpob should fail if there's not trace of PPOB in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LPpob]()

      service.submitPpob() failedWith classOf[IllegalStateException]
    }

    "submitPpob should succeed when S4LPpob in S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertPpob(any(), any())(any(), any())).thenReturn(scrsAddress.pure)
      save4laterReturns(S4LPpob(Some(PpobView("1", Some(scrsAddress)))))

      service.submitPpob() returns scrsAddress
    }

    "submitSicAndCompliance should fail if VatSicAndCompliance not in backend and S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatSicAndCompliance]()

      service.submitSicAndCompliance() failedWith classOf[IllegalStateException]
    }
  }
}
