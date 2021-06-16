/*
 * Copyright 2021 HM Revenue & Customs
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

import models.api.SicCode
import models._
import models.api.returns.Returns
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import testHelpers.VatSpec
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.Future

class FlatRateServiceSpec extends VatSpec {

  class Setup {
    val service = new FlatRateService(
      mockS4LService,
      mockSicAndComplianceService,
      mockConfigConnector,
      mockVatRegistrationConnector
    )
  }

  val financialsWithEstimateVatTurnoverIs100000: TurnoverEstimates = TurnoverEstimates(turnoverEstimate = 100000L)

  "applyPercentRoundUp" should {
    "get 2% of the total sales estimate and round up to the nearest pound" in new Setup() {
      val testEstimate = 951
      val testResult = 20

      service.applyPercentRoundUp(testEstimate) mustBe testResult
    }
  }

  "getFlatRate" should {
    val frSch = FlatRateScheme(
      Some(false)
    )

    "return the S4L model if it is there" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(frSch)))

      await(service.getFlatRate) mustBe frSch
    }

    "return the converted backend model if the S4L is not there" in new Setup() {

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(Some(frSch)))

      await(service.getFlatRate) mustBe frSch
    }

    "return an empty backend model if the S4L is not there and neither is the backend" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(None))

      await(service.getFlatRate) mustBe FlatRateScheme()
    }
  }

  "handleView" should {
    "return a Complete model for a full model with business goods and start date and uses rate" in new Setup {
      val data = FlatRateScheme(Some(true), Some(true), Some(1000L), Some(true), Some(true), frsDate, Some("frsId"), Some(10.5), Some(false))

      service.handleView(data) mustBe Complete(data)
    }

    "return a Complete model for a full model without business goods and start date and uses rate" in new Setup {
      val data = FlatRateScheme(Some(true), Some(false), Some(150L), Some(true), Some(true), frsDate, Some("frsId"), Some(10.5), Some(false))
      val expected = FlatRateScheme(Some(true), Some(false), None, None, Some(true), frsDate, Some("frsId"), Some(10.5), Some(true))

      service.handleView(data) mustBe Complete(expected)
    }

    "return a Complete model for a full model with business goods and no 'uses this rate'" in new Setup {
      val data = FlatRateScheme(Some(false), Some(true), Some(1000L), Some(true), Some(false), None, Some("frsId"), Some(10.5), Some(false))

      service.handleView(data) mustBe Complete(data)
    }

    "return a Complete model for a full model without business goods and no 'uses this rate'" in new Setup {
      val data = FlatRateScheme(Some(false), Some(false), None, None, Some(false), None, Some("frsId"), Some(10.5), Some(true))

      service.handleView(data) mustBe Complete(data)
    }

    "return a Complete model for a full model with business goods but no over business goods percent and uses rate" in new Setup {
      val data = FlatRateScheme(Some(true), Some(true), Some(1000L), Some(false), Some(true), frsDate, Some("frsId"), Some(10.5), Some(true))

      service.handleView(data) mustBe Complete(data)
    }

    "return a Complete model for a full model with business goods but no over business goods percent and no 'uses this rate'" in new Setup {
      val data = FlatRateScheme(Some(false), Some(true), Some(1000L), Some(false), Some(false), None, Some("frsId"), Some(10.5), Some(true))

      service.handleView(data) mustBe Complete(data)
    }

    "convert a S4L model (does not join)" in new Setup() {
      service.handleView(frsNoJoin) mustBe Complete(frsNoJoin)
    }

    "convert a S4L model (does not join) with details block completed" in new Setup() {
      service.handleView(frsNoJoinWithDetails) mustBe Complete(frsNoJoinWithDetails)
    }

    "do not handle an incomplete" in new Setup() {
      service.handleView(incompleteS4l) mustBe Incomplete(incompleteS4l)
    }

    "do not handle an empty S4L" in new Setup() {
      service.handleView(FlatRateScheme()) mustBe Incomplete(FlatRateScheme())
    }
  }

  "submitFlatRate" should {
    "if the S4L model is incomplete, save to S4L" in new Setup() {
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.submitFlatRate(incompleteS4l)) mustBe incompleteS4l
    }

    "if the S4L model is complete, save to the backend and clear S4L" in new Setup() {
      when(mockVatRegistrationConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200, "")))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.submitFlatRate(frs1KReg)) mustBe frs1KReg
    }
  }

  "saveJoiningFRS" should {
    "save joining the FRS" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(FlatRateScheme())))
      when(mockVatRegistrationConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(None))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveJoiningFRS(answer = true)) mustBe FlatRateScheme(joinFrs = Some(true))
    }

    "save not joining the FRS (and save to backend)" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(FlatRateScheme())))
      when(mockVatRegistrationConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(None))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockVatRegistrationConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200, "")))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveJoiningFRS(answer = false)) mustBe FlatRateScheme(joinFrs = Some(false))
    }
  }

  "saveOverBusinessGoods" should {
    "save that they have gone over" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverBusinessGoods(true)) mustBe
        incompleteS4l.copy(overBusinessGoods = Some(true), limitedCostTrader = Some(false))
    }

    "save that they do not go over" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverBusinessGoods(false)) mustBe
        incompleteS4l.copy(overBusinessGoods = Some(false), limitedCostTrader = Some(true))
    }
  }

  "saveOverBusinessGoodsPercent" should {
    "save that they have gone over" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverBusinessGoodsPercent(true)) mustBe
        incompleteS4l.copy(overBusinessGoodsPercent = Some(true), limitedCostTrader = Some(false))
    }

    "save that they do not go over" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverBusinessGoodsPercent(false)) mustBe
        incompleteS4l.copy(overBusinessGoodsPercent = Some(false), limitedCostTrader = Some(true))
    }
  }

  "saveRegister" should {
    val testSicCode = "testSicCode"
    val testCategory = "testCategory"
    val testType = "testType"
    val testPercent: BigDecimal = 10.5

    "save that they want to register without business goods, keeping category and percentage" in new Setup() {
      val data: FlatRateScheme = incompleteS4l.copy(overBusinessGoods = Some(false))

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(data)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(SicAndCompliance(mainBusinessActivity = Some(MainBusinessActivityView(id = testSicCode)))))
      when(mockConfigConnector.getSicCodeFRSCategory(any())).thenReturn(testCategory)
      when(mockConfigConnector.getBusinessTypeDetails(any())).thenReturn((testType, testPercent))

      await(service.saveRegister(answer = true)) mustBe data.copy(useThisRate = Some(true), categoryOfBusiness = Some(testCategory), percent = Some(testPercent))
    }

    "save that they want to register with business goods, keeping category and percentage" in new Setup() {
      val data: FlatRateScheme = incompleteS4l.copy(estimateTotalSales = Some(1000L), overBusinessGoodsPercent = Some(false))

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(data)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(SicAndCompliance(mainBusinessActivity = Some(MainBusinessActivityView(id = testSicCode)))))
      when(mockConfigConnector.getSicCodeFRSCategory(any())).thenReturn(testCategory)
      when(mockConfigConnector.getBusinessTypeDetails(any())).thenReturn((testType, testPercent))

      await(service.saveRegister(answer = true)) mustBe
        data.copy(useThisRate = Some(true), categoryOfBusiness = Some(testCategory), percent = Some(testPercent))
    }

    "save that they do not wish to register (clearing the start date, saving to the backend), keeping category and percentage" in new Setup() {
      val data: FlatRateScheme = incompleteS4l.copy(overBusinessGoods = Some(false), useThisRate = Some(true), frsStart = Some(Start(Some(LocalDate.of(2017, 10, 10)))))

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(data)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockVatRegistrationConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200, "")))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(SicAndCompliance(mainBusinessActivity = Some(MainBusinessActivityView(id = testSicCode)))))
      when(mockConfigConnector.getSicCodeFRSCategory(any())).thenReturn(testCategory)
      when(mockConfigConnector.getBusinessTypeDetails(any())).thenReturn((testType, testPercent))

      await(service.saveRegister(answer = false)) mustBe
        data.copy(joinFrs = Some(false), useThisRate = Some(false), categoryOfBusiness = Some(testCategory), percent = Some(testPercent), frsStart = None)
    }
  }

  "retrieveSectorPercent" should {
    val s4LVatSicAndComplianceNoMainBusinessActivity = s4lVatSicAndComplianceWithLabour.copy(mainBusinessActivity = None)

    "retrieve a retrieveSectorPercent if one is saved" in new Setup {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(frs1KReg)))
      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(("test business type", BigDecimal(6.32)))

      await(service.retrieveSectorPercent) mustBe("frsId", "test business type", BigDecimal(6.32))
    }

    "determine a retrieveSectorPercent if none is saved but main business activity is known" in new Setup {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))

      when(mockConfigConnector.getSicCodeFRSCategory(any()))
        .thenReturn("frsId")

      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(service.retrieveSectorPercent) mustBe("frsId", validBusinessSectorView._1, validBusinessSectorView._2)
    }

    "fail if no retrieveSectorPercent is saved and main business activity is not known" in new Setup {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndComplianceNoMainBusinessActivity))

      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(validBusinessSectorView)

      val exception: IllegalStateException = intercept[IllegalStateException](await(service.retrieveSectorPercent))

      exception.getMessage mustBe "[FlatRateService] [retrieveSectorPercent] Can't determine main business activity"
    }
  }

  "saveUseFlatRate" should {
    "save the flat rate percentage and Use this rate" when {
      "user selects Yes" in new Setup {
        when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = Some("frsId"), percent = None))))
        when(mockS4LService.save(any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(dummyCacheMap))
        when(mockConfigConnector.getSicCodeFRSCategory(any()))
          .thenReturn("frsId")
        when(mockConfigConnector.getBusinessTypeDetails(any()))
          .thenReturn(("test", defaultFlatRate))

        await(service.saveUseFlatRate(answer = true)) mustBe incompleteS4l.copy(
          joinFrs = Some(true),
          useThisRate = Some(true),
          categoryOfBusiness = Some("frsId"),
          percent = Some(defaultFlatRate)
        )
      }

      "user selects No, sets joinFRS to false, model is now complete" in new Setup {
        when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = Some("frsId"), percent = None))))
        when(mockConfigConnector.getSicCodeFRSCategory(any()))
          .thenReturn("frsId")
        when(mockConfigConnector.getBusinessTypeDetails(any()))
          .thenReturn(("test", defaultFlatRate))
        when(mockVatRegistrationConnector.upsertFlatRate(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))
        when(mockS4LService.clearKey(any(), any(), any()))
          .thenReturn(Future.successful(dummyCacheMap))

        await(service.saveUseFlatRate(answer = false)) mustBe incompleteS4l.copy(
          joinFrs = Some(false),
          useThisRate = Some(false),
          categoryOfBusiness = Some("frsId"),
          percent = Some(defaultFlatRate),
          limitedCostTrader = Some(false)
        )
      }
    }
  }

  "getPrepopulatedStartDate" should {
    "should get an empty model if there is nothing in S4L" in new Setup() {
      val vatStartDate: Option[LocalDate] = validVatScheme.returns.get.startDate
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(None, None)
    }

    "should get as different date if it does not match the vat start date" in new Setup() {
      val diffdate: LocalDate = LocalDate.of(2017, 11, 11)
      val vatStartDate: Option[LocalDate] = validVatScheme.returns.get.startDate

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(
          Some(incompleteS4l.copy(frsStart = Some(Start(Some(diffdate)))))
        ))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(Some(FRSDateChoice.DifferentDate), Some(diffdate))
    }

    "should get vat date if it matches the vat start date" in new Setup() {
      val vatStartDate: Option[LocalDate] = validVatScheme.returns.get.startDate
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(
          Some(incompleteS4l.copy(frsStart = Some(Start(None))))
        ))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(Some(FRSDateChoice.VATDate), None)
    }
  }

  "saveConfirmSector" should {
    "save sector" when {
      "lookup main business activity returns a correct sector & user has not selected a sector before" in new Setup {
        when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = None, percent = None))))
        when(mockS4LService.save(any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(dummyCacheMap))
        when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
          .thenReturn(Future.successful(SicAndCompliance(mainBusinessActivity = Some(MainBusinessActivityView(id = "sic123")))))
        when(mockConfigConnector.getSicCodeFRSCategory(any())).thenReturn("test321")
        when(mockConfigConnector.getBusinessTypeDetails(any())).thenReturn(("test321", BigDecimal(1.00)))

        await(service.saveConfirmSector) mustBe incompleteS4l.copy(categoryOfBusiness = Some("test321"))
      }
      "lookup main busines activity returns a correct sector & user has full model, sector & percent is the same as previously selected" in new Setup() {
        when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate)))))
        when(mockS4LService.save(any())(any(), any(), any(), any()))
          .thenReturn(Future.successful(dummyCacheMap))
        when(mockConfigConnector.getBusinessTypeDetails(any()))
          .thenReturn(("test business type", BigDecimal(6.32)))

        await(service.saveConfirmSector) mustBe
          incompleteS4l.copy(categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate))
      }
    }
  }

  "saveStartDate" should {
    "save that the start date should be the vat start date" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      await(service.saveStartDate(FRSDateChoice.VATDate, None)) mustBe
        incompleteS4l.copy(frsStart = Some(Start(validVatScheme.returns.get.startDate)))
    }

    "save that the start date should be a different date" in new Setup() {
      val frsStart: LocalDate = LocalDate.of(2017, 10, 10)

      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveStartDate(FRSDateChoice.DifferentDate, Some(frsStart))) mustBe
        incompleteS4l.copy(frsStart = frsDate)
    }
  }

  "fetchVatStartDate" should {
    "return vat start date when it exists" in new Setup() {
      val returns: Returns = validVatScheme.returns.get
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(returns))

      await(service.fetchVatStartDate) mustBe returns.startDate
    }
    "return None when date does not exist" in new Setup() {
      val returns: Returns = validVatScheme.returns.get.copy(startDate = None)
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(returns))

      await(service.fetchVatStartDate) mustBe None
    }
    "return an exception when getReturns returns an exception" in new Setup() {
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.failed(new Exception("")))

      intercept[Exception](await(service.fetchVatStartDate))
    }
  }

  "resetFRS" should {
    "reset Flat Rate Scheme if the Main Business Activity Sic Code has changed" in new Setup {
      val newSicCode = SicCode("newId", "new Desc", "new Details")

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      when(mockS4LService.save[FlatRateScheme](any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockVatRegistrationConnector.clearFlatRate(any())(any()))
        .thenReturn(Future.successful(HttpResponse(200, "")))

      await(service.resetFRSForSAC(newSicCode)) mustBe newSicCode
    }
  }
  "clearFrs" should {
    "reset Flat Rate Scheme and return true" in new Setup {
      when(mockS4LService.save[FlatRateScheme](any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockVatRegistrationConnector.clearFlatRate(any())(any()))
        .thenReturn(Future.successful(HttpResponse(200, "")))

      await(service.clearFrs) mustBe true
      verify(mockS4LService, times(1)).save(any())(any(), any(), any(), any())
      verify(mockVatRegistrationConnector, times(1)).clearFlatRate(any())(any())
    }
  }

  "saveEstimateTotalSales" should {
    "save estimated total sales" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveEstimateTotalSales(30000L)) mustBe
        incompleteS4l.copy(estimateTotalSales = Some(30000L))
    }
  }

  "saveBusinessType" should {
    "save business type and reset percent if business type is different" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some("001"), percent = Some(10.5)))))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(("003", BigDecimal(3.0)))

      await(service.saveBusinessType("003")) mustBe incompleteS4l.copy(useThisRate = None, categoryOfBusiness = Some("003"), percent = None)
    }

    "not change business type but resets percent if the percent definition has changed" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some("001"), percent = Some(10.5)))))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(("001", BigDecimal(3.0)))

      await(service.saveBusinessType("001")) mustBe incompleteS4l.copy(useThisRate = None, categoryOfBusiness = Some("001"), percent = None)
    }

    "not change business type and does not reset percent if same as before" in new Setup() {
      when(mockS4LService.fetchAndGet[FlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some("001"), percent = Some(10.5)))))
      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockConfigConnector.getBusinessTypeDetails(any()))
        .thenReturn(("001", BigDecimal(10.5)))

      await(service.saveBusinessType("001")) mustBe incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some("001"), percent = Some(10.5))
    }
  }
}
