/*
 * Copyright 2022 HM Revenue & Customs
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

import _root_.models._
import connectors.mocks.MockRegistrationApiConnector
import featureswitch.core.config.FeatureSwitching
import models.api.vatapplication._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import play.api.libs.json.{JsString, Reads, Writes}
import services.VatApplicationService._
import services.mocks.MockVatRegistrationService
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import java.time.LocalDate
import scala.concurrent.Future

class VatApplicationServiceSpec extends VatRegSpec with FeatureSwitching with MockRegistrationApiConnector with MockVatRegistrationService {

  class Setup {
    object Service extends VatApplicationService(
      mockRegistrationApiConnector,
      vatRegistrationServiceMock,
      mockS4LService,
      mockApplicantDetailsServiceOld,
      mockTimeService
    )
  }

  override def beforeEach(): Unit = {
    reset(
      mockRegistrationApiConnector,
      vatRegistrationServiceMock,
      mockS4LService
    )
  }

  val mockCacheMap: CacheMap = CacheMap("", Map("" -> JsString("")))

  val date: LocalDate = LocalDate.now
  override val validVatApplication: VatApplication = VatApplication(
    tradeVatGoodsOutsideUk = Some(false),
    eoriRequested = Some(false),
    turnoverEstimate = Some(testTurnover),
    zeroRatedSupplies = Some(testZeroRatedSupplies),
    claimVatRefunds = Some(true),
    appliedForExemption = None,
    startDate = Some(date),
    returnsFrequency = Some(Quarterly),
    staggerStart = Some(FebruaryStagger)
  )
  val vatApplicationFixed: VatApplication = validVatApplication.copy(startDate = Some(LocalDate.of(2017, 12, 25)))
  val vatApplicationAlt: VatApplication = validVatApplication.copy(startDate = Some(LocalDate.of(2017, 12, 12)))
  val testAASDetails: AASDetails = AASDetails(Some(MonthlyPayment), Some(BankGIRO))
  val testAASVatApplication: VatApplication = validVatApplication.copy(returnsFrequency = Some(Annual), staggerStart = Some(JanDecStagger), annualAccountingDetails = Some(testAASDetails))
  val testWarehouseNumber = "asd123456789123"
  val testWarehouseName = "testWarehouseName"
  val testOverseasCompliance: OverseasCompliance = OverseasCompliance(Some(true), Some(true), Some(StoringWithinUk), Some(true), Some(testWarehouseNumber), Some(testWarehouseName))
  val testOverseasVatApplication: VatApplication = validVatApplication.copy(overseasCompliance = Some(testOverseasCompliance))
  val emptyVatApplication: VatApplication = VatApplication()

  def mockS4LSave(vatApplication: VatApplication): OngoingStubbing[Future[CacheMap]] =
    when(mockS4LService.save(ArgumentMatchers.eq(vatApplication))(
      any[S4LKey[VatApplication]](),
      any[CurrentProfile](),
      any[HeaderCarrier](),
      any[Writes[VatApplication]]()
    )).thenReturn(Future.successful(mockCacheMap))

  def mockS4LClear: OngoingStubbing[Future[CacheMap]] =
    when(mockS4LService.clearKey(any(), any(), any()))
      .thenReturn(Future.successful(mockCacheMap))

  def mockS4LGet(vatApplication: Option[VatApplication]): OngoingStubbing[Future[Option[VatApplication]]] =
    when(mockS4LService.fetchAndGet[VatApplication](
      any[S4LKey[VatApplication]](),
      any[CurrentProfile](),
      any[HeaderCarrier](),
      any[Reads[VatApplication]]()
    )).thenReturn(Future.successful(vatApplication))

  "getVatApplication" must {
    "return a full VatApplication view model from backend" in new Setup {
      mockS4LGet(Some(validVatApplication))

      await(Service.getVatApplication) mustBe validVatApplication
    }

    "return a full VatApplication view model from s4l" in new Setup {
      mockS4LGet(None)

      mockGetSection[VatApplication](testRegId, Some(validVatApplication))

      await(Service.getVatApplication) mustBe validVatApplication
    }
  }

  "saveVatApplication" must {
    "return a VatApplication" when {
      "updating with tradeVatGoodsOutsideUK" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(tradeVatGoodsOutsideUk = Some(true))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(TradeVatGoodsOutsideUk(true))) mustBe expected
      }
      "updating with eoriRequested" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(eoriRequested = Some(true))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(EoriRequested(true))) mustBe expected
      }
      "updating with turnoverEstimate" in new Setup() {
          val expected: VatApplication = emptyVatApplication.copy(turnoverEstimate = Some(testTurnover))

          mockS4LGet(Some(emptyVatApplication))
          mockGetSection[VatApplication](testRegId, None)
          mockReplaceSection(testRegId, expected)
          mockS4LClear

          await(Service.saveVatApplication(Turnover(testTurnover))) mustBe expected
        }
        "updating with turnoverEstimate while removing AAS, FRS and Exemption if it exceeds thresholds" in new Setup() {
          val largeTurnover = 1350001
          val expected: VatApplication = testAASVatApplication.copy(
            turnoverEstimate = Some(largeTurnover),
            appliedForExemption = None,
            returnsFrequency = None,
            annualAccountingDetails = None
          )

          mockS4LGet(Some(testAASVatApplication))
          mockDeleteSection[FlatRateScheme](testRegId)
          mockReplaceSection(testRegId, expected)
          mockS4LClear

          await(Service.saveVatApplication(Turnover(largeTurnover))) mustBe expected
        }

      "updating with zeroRatedSupplies" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(zeroRatedSupplies = Some(testZeroRatedSupplies))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(ZeroRated(testZeroRatedSupplies))) mustBe expected
      }
      "updating with zeroRatedSupplies while removing exemption answer if zeroRatedSupplies is too small" in new Setup() {
        val testVatApplication = validVatApplication.copy(appliedForExemption = Some(true))
        val expected: VatApplication = testVatApplication.copy(
          zeroRatedSupplies = Some(testTurnover / 2),
          appliedForExemption = None
        )

        mockS4LGet(Some(testVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(ZeroRated(testTurnover / 2))) mustBe expected
      }
      "updating with claimVatRefunds" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(claimVatRefunds = Some(true))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(ClaimVatRefunds(true))) mustBe expected
      }
      "updating with claimVatRefunds while removing exemption answer if claimVatRefunds is false" in new Setup() {
        val testVatApplication = validVatApplication.copy(appliedForExemption = Some(true))
        val expected: VatApplication = testVatApplication.copy(
          claimVatRefunds = Some(false),
          appliedForExemption = None
        )

        mockS4LGet(Some(testVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(ClaimVatRefunds(false))) mustBe expected
      }
      "updating with appliedForExemption" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(appliedForExemption = Some(true))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(AppliedForExemption(true))) mustBe expected
      }
      "updating with startDate" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(startDate = Some(testDate))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(testDate)) mustBe expected
      }
      "updating with returnsFrequency" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(returnsFrequency = Some(Quarterly), staggerStart = None)

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(Quarterly)) mustBe expected
      }
      "updating with returnsFrequency while hardcoding Monthly stagger" in new Setup() {
        val expected: VatApplication = testAASVatApplication.copy(
          returnsFrequency = Some(Monthly),
          staggerStart = Some(MonthlyStagger),
          annualAccountingDetails = None
        )

        mockS4LGet(Some(validVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(Monthly)) mustBe expected
      }
      "updating with staggerStart" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(staggerStart = Some(JanDecStagger))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(JanDecStagger)) mustBe expected
      }
      "updating with staggerStart and returnsFrequency if stagger is quarterly" in new Setup() {
        val expected: VatApplication = testAASVatApplication.copy(
          staggerStart = Some(FebruaryStagger),
          returnsFrequency = Some(Quarterly),
          annualAccountingDetails = None
        )

        mockS4LGet(Some(testAASVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(FebruaryStagger)) mustBe expected
      }
      "updating with hasTaxRepresentative" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(hasTaxRepresentative = Some(true))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(HasTaxRepresentative(true))) mustBe expected
      }
      "updating with NIP answer goodsToEu" in new Setup() {
        val testConditionalTurnover = ConditionalValue(answer = true, Some(testTurnover))
        val expected: VatApplication = emptyVatApplication.copy(northernIrelandProtocol = Some(NIPTurnover(Some(testConditionalTurnover))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(TurnoverToEu(testConditionalTurnover))) mustBe expected
      }
      "updating with NIP answer goodsFromEu" in new Setup() {
        val testConditionalTurnover = ConditionalValue(answer = true, Some(testTurnover))
        val expected: VatApplication = emptyVatApplication.copy(northernIrelandProtocol = Some(NIPTurnover(goodsFromEU = Some(testConditionalTurnover))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(TurnoverFromEu(testConditionalTurnover))) mustBe expected
      }
      "updating with Overseas answer goodsToOverseas" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(goodsToOverseas = Some(true))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(GoodsToOverseas(true))) mustBe expected
      }
      "updating with Overseas answer goodsToOverseas while removing goodsToEu if false" in new Setup() {
        val expected: VatApplication = testOverseasVatApplication.copy(
          overseasCompliance = Some(testOverseasCompliance.copy(goodsToOverseas = Some(false), goodsToEu = None))
        )

        mockS4LGet(Some(testOverseasVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(GoodsToOverseas(false))) mustBe expected
      }
      "updating with Overseas answer goodsToEu" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(goodsToEu = Some(true))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(GoodsToEu(true))) mustBe expected
      }
      "updating with Overseas answer storingGoodsForDispatch" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(storingGoodsForDispatch = Some(StoringWithinUk))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(StoringWithinUk)) mustBe expected
      }
      "updating with Overseas answer storingGoodsForDispatch while removing warehouse info if storing overseas" in new Setup() {
        val expected: VatApplication = testOverseasVatApplication.copy(
          overseasCompliance = Some(testOverseasCompliance.copy(
            storingGoodsForDispatch = Some(StoringOverseas),
            usingWarehouse = None,
            fulfilmentWarehouseNumber = None,
            fulfilmentWarehouseName = None
          ))
        )

        mockS4LGet(Some(testOverseasVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(StoringOverseas)) mustBe expected
      }
      "updating with Overseas answer usingWarehouse" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(usingWarehouse = Some(true))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(UsingWarehouse(true))) mustBe expected
      }
      "updating with Overseas answer usingWarehouse while removing warehouseName and warehouseNumber if false" in new Setup() {
        val expected: VatApplication = testOverseasVatApplication.copy(
          overseasCompliance = Some(testOverseasCompliance.copy(
            usingWarehouse = Some(false),
            fulfilmentWarehouseNumber = None,
            fulfilmentWarehouseName = None
          ))
        )

        mockS4LGet(Some(testOverseasVatApplication))
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(UsingWarehouse(false))) mustBe expected
      }
      "updating with Overseas answer warehouseNumber" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(fulfilmentWarehouseNumber = Some(testWarehouseNumber))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(WarehouseNumber(testWarehouseNumber))) mustBe expected
      }
      "updating with Overseas answer warehouseName" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(overseasCompliance = Some(OverseasCompliance(fulfilmentWarehouseName = Some(testWarehouseName))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(WarehouseName(testWarehouseName))) mustBe expected
      }
      "updating with AAS answer paymentFrequency" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(MonthlyPayment)) mustBe expected
      }
      "updating with AAS answer paymentMethod" in new Setup() {
        val expected: VatApplication = emptyVatApplication.copy(annualAccountingDetails = Some(AASDetails(paymentMethod = Some(CHAPS))))

        mockS4LGet(Some(emptyVatApplication))
        mockGetSection[VatApplication](testRegId, None)
        mockReplaceSection(testRegId, expected)
        mockS4LClear

        await(Service.saveVatApplication(CHAPS)) mustBe expected
      }
    }
  }

  "getTurnover" must {
    "return turnover from VatApplication" in new Setup {
      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(validVatApplication)))

      await(Service.getTurnover) mustBe Some(testTurnover)
    }
  }

  "retrieveCalculatedStartDate" must {
    "return the calculated date from EligibilitySubmissionData" in new Setup {
      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(testDate))))

      await(Service.retrieveCalculatedStartDate) mustBe testDate
    }

    "throw a InternalServerException when calculated date is missing" in new Setup {
      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = None)))

      intercept[InternalServerException](await(Service.retrieveCalculatedStartDate)).message mustBe "[VatApplicationService] Missing calculated date"
    }
  }

  "retrieveMandatoryDates" must {
    val calculatedDate: LocalDate = LocalDate.of(2017, 12, 25)

    "return a full MandatoryDateModel with a selection of calculated_date if the vatStartDate is present and is equal to the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 25)

      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(vatApplicationFixed)))

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(Service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.calculated_date))
    }

    "return a full MandatoryDateModel with a selection of specific_date if the vatStartDate does not equal the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 12)

      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(vatApplicationAlt)))

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(Service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.specific_date))
    }

    "return a MandatoryDateModel with just a calculated date if the vatStartDate is not present" in new Setup {
      mockS4LGet(Some(emptyVatApplication))
      mockGetSection[VatApplication](testRegId, None)

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(Service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, None, None)
    }
  }

  "saveVoluntaryStartDate" must {
    "save a company start date as the vat start date" in new Setup {
      val expected: VatApplication = emptyVatApplication.copy(startDate = Some(testIncorpDate))

      mockS4LGet(Some(emptyVatApplication))
      mockGetSection[VatApplication](testRegId, None)
      mockReplaceSection(testRegId, expected)
      mockS4LClear

      await(Service.saveVoluntaryStartDate(
        DateSelection.company_registration_date, None, testIncorpDate
      )) mustBe expected
    }

    "save a specific start date" in new Setup {
      val specificStartDate: LocalDate = LocalDate.of(2017, 12, 12)
      val expected: VatApplication = emptyVatApplication.copy(startDate = Some(specificStartDate))

      mockS4LGet(Some(emptyVatApplication))
      mockGetSection[VatApplication](testRegId, None)
      mockReplaceSection[VatApplication](testRegId, expected)
      mockS4LClear

      await(Service.saveVoluntaryStartDate(
        DateSelection.specific_date, Some(specificStartDate), testIncorpDate
      )) mustBe expected
    }
  }

  "isEligibleForAAS" must {
    val validTurnover = 1350000L
    val invalidTurnover = 1350001L

    "return false for a turnover that is above 1350000" in new Setup {
      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(validVatApplication.copy(turnoverEstimate = Some(invalidTurnover)))))

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      await(Service.isEligibleForAAS) mustBe false
    }

    "return false for a Groups Registration" in new Setup {
      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(validVatApplication.copy(turnoverEstimate = Some(validTurnover)))))

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(registrationReason = GroupRegistration)))

      await(Service.isEligibleForAAS) mustBe false
    }

    "return true when the turnover estimate is valid for AAS" in new Setup {
      when(mockS4LService.fetchAndGet[VatApplication](any[S4LKey[VatApplication]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(validVatApplication.copy(turnoverEstimate = Some(validTurnover)))))

      when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      await(Service.isEligibleForAAS) mustBe true
    }
  }
}
