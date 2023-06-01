/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.mocks.MockRegistrationApiConnector
import models._
import models.api.SicCode
import models.api.vatapplication.VatApplication
import models.error.MissingAnswerException
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import services.FlatRateService.{CategoryOfBusinessAnswer, EstimateTotalSalesAnswer, JoinFrsAnswer, OverBusinessGoodsAnswer, OverBusinessGoodsPercentAnswer, UseThisRateAnswer}
import testHelpers.VatSpec
import uk.gov.hmrc.http.InternalServerException
import play.api.mvc.Request
import play.api.test.FakeRequest
import java.time.LocalDate
import scala.concurrent.Future

class FlatRateServiceSpec extends VatSpec with MockRegistrationApiConnector {

  implicit val fakeRequest: Request[_] = FakeRequest()

  class Setup {
    val service = new FlatRateService(
      mockBusinessService,
      movkVatApplicationService,
      mockConfigConnector,
      mockRegistrationApiConnector
    )
  }

  "applyPercentRoundUp" must {
    "get 2% of the total sales estimate and round up to the nearest pound" in new Setup() {
      val testEstimate = 951
      val testResult = 20

      service.applyPercentRoundUp(testEstimate) mustBe testResult
    }
  }

  "getFlatRate" must {
    val frSch = FlatRateScheme(
      Some(false)
    )
    "return the backend model" in new Setup() {
      mockGetSection[FlatRateScheme](testRegId, Some(frSch))

      await(service.getFlatRate) mustBe frSch
    }

    "return an empty model if data isn't in backend" in new Setup() {
      mockGetSection[FlatRateScheme](testRegId, None)

      await(service.getFlatRate) mustBe FlatRateScheme()
    }
  }

  "saveFlatRate" must {
    "save JoinFrsAnswer and return scheme" when {
      "the answer is true" in new Setup {
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        mockReplaceSection[FlatRateScheme](testRegId, validFlatRate)

        await(service.saveFlatRate(JoinFrsAnswer(true))) mustBe validFlatRate
      }

      "the answer is false" in new Setup {
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        mockReplaceSection[FlatRateScheme](testRegId, FlatRateScheme(Some(false)))

        await(service.saveFlatRate(JoinFrsAnswer(false))) mustBe FlatRateScheme(Some(false))
      }
    }

    "save OverBusinessGoodsAnswer and return scheme" when {
      val testFrs = FlatRateScheme(Some(true))
      val testFrsLimitedCost = FlatRateScheme(Some(true), Some(false), None, None, Some(true), Some(frsDate), Some(testBusinessCategory), Some(flatRatePercentage), Some(true))
      "the answer is true" in new Setup {
        val newFlatRate: FlatRateScheme = testFrs.copy(overBusinessGoods = Some(true), limitedCostTrader = Some(false))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrs))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsAnswer(true))) mustBe newFlatRate
      }

      "the answer is true and there are previous answers for limited cost trader" in new Setup {
        val newFlatRate: FlatRateScheme = testFrsLimitedCost.copy(overBusinessGoods = Some(true), useThisRate = None, limitedCostTrader = Some(false))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrsLimitedCost))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsAnswer(true))) mustBe newFlatRate
      }

      "the answer is false" in new Setup {
        val newFlatRate: FlatRateScheme = testFrs.copy(overBusinessGoods = Some(false), limitedCostTrader = Some(true))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrs))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsAnswer(false))) mustBe newFlatRate
      }

      "the answer is false and there are previous answers for non limited cost trader" in new Setup {
        val newFlatRate: FlatRateScheme = validFlatRate.copy(
          overBusinessGoods = Some(false),
          limitedCostTrader = Some(true),
          overBusinessGoodsPercent = None,
          estimateTotalSales = None,
          categoryOfBusiness = None,
          percent = None,
          useThisRate = None
        )
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsAnswer(false))) mustBe newFlatRate
      }
    }

    "save EstimateTotalSalesAnswer and return scheme" in new Setup {
      val testEstimate = 123
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(estimateTotalSales = Some(testEstimate)))

      await(service.saveFlatRate(EstimateTotalSalesAnswer(testEstimate))) mustBe validFlatRate.copy(estimateTotalSales = Some(testEstimate))
    }

    "save OverBusinessGoodsPercentAnswer and return scheme" when {
      val testFrs = FlatRateScheme(Some(true), Some(true), Some(testTurnover))
      val testFrsLimitedCost = FlatRateScheme(Some(true), Some(true), Some(testTurnover), Some(false), Some(true), Some(frsDate), Some(testBusinessCategory), Some(flatRatePercentage), Some(true))
      "the answer is true" in new Setup {
        val newFlatRate: FlatRateScheme = testFrs.copy(overBusinessGoodsPercent = Some(true), limitedCostTrader = Some(false))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrs))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsPercentAnswer(true))) mustBe newFlatRate
      }

      "the answer is true and there are previous answers for limited cost trader" in new Setup {
        val newFlatRate: FlatRateScheme = testFrsLimitedCost.copy(overBusinessGoodsPercent = Some(true), useThisRate = None, limitedCostTrader = Some(false))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrsLimitedCost))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsPercentAnswer(true))) mustBe newFlatRate
      }

      "the answer is false" in new Setup {
        val newFlatRate: FlatRateScheme = testFrs.copy(overBusinessGoodsPercent = Some(false), limitedCostTrader = Some(true))
        mockGetSection[FlatRateScheme](testRegId, Some(testFrs))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsPercentAnswer(false))) mustBe newFlatRate
      }

      "the answer is false and there are previous answers for non limited cost trader" in new Setup {
        val newFlatRate: FlatRateScheme = validFlatRate.copy(
          overBusinessGoodsPercent = Some(false),
          limitedCostTrader = Some(true),
          categoryOfBusiness = None,
          percent = None,
          useThisRate = None
        )
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        mockReplaceSection[FlatRateScheme](testRegId, newFlatRate)

        await(service.saveFlatRate(OverBusinessGoodsPercentAnswer(false))) mustBe newFlatRate
      }
    }

    "save UseThisRateAnswer and return scheme" when {
      "the answer is true" in new Setup {
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        when(mockConfigConnector.getBusinessType(any())(any[Request[_]]))
          .thenReturn(testBusinessTypeDetails)
        mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(useThisRate = Some(true), percent = Some(testBusinessTypeDetails.percentage)))

        await(service.saveFlatRate(UseThisRateAnswer(true))) mustBe validFlatRate.copy(useThisRate = Some(true), percent = Some(testBusinessTypeDetails.percentage))
      }

      "the answer is false" in new Setup {
        mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
        mockReplaceSection[FlatRateScheme](testRegId, FlatRateScheme(Some(false)))

        await(service.saveFlatRate(UseThisRateAnswer(false))) mustBe FlatRateScheme(Some(false))
      }
    }

    "save FRS Start date and return scheme" in new Setup {
      val testDate: LocalDate = LocalDate.now().minusMonths(2)
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(frsStart = Some(testDate)))

      await(service.saveFlatRate(testDate)) mustBe validFlatRate.copy(frsStart = Some(testDate))
    }

    "save CategoryOfBusinessAnswer and return scheme" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(categoryOfBusiness = Some(testBusinessCategory), percent = None, useThisRate = None))

      await(service.saveFlatRate(CategoryOfBusinessAnswer(testBusinessCategory))) mustBe validFlatRate.copy(categoryOfBusiness = Some(testBusinessCategory), percent = None, useThisRate = None)
    }
  }

  "saveRegister" must {
    "save answer and category with percentage if true" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate))
      when(mockBusinessService.getBusiness(any(), any()))
        .thenReturn(Future.successful(validBusiness))
      when(mockConfigConnector.getSicCodeFRSCategory(any())(any()))
        .thenReturn(testBusinessCategory)
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)
      mockReplaceSection[FlatRateScheme](testRegId, incompleteFlatRate.copy(useThisRate = Some(true), categoryOfBusiness = Some(testBusinessCategory), percent = Some(testBusinessTypeDetails.percentage)))

      await(service.saveRegister(answer = true)) mustBe incompleteFlatRate.copy(useThisRate = Some(true), categoryOfBusiness = Some(testBusinessCategory), percent = Some(testBusinessTypeDetails.percentage))
    }

    "clear the frs scheme if answer is false" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      mockReplaceSection[FlatRateScheme](testRegId, FlatRateScheme(Some(false)))

      await(service.saveRegister(answer = false)) mustBe FlatRateScheme(Some(false))
    }
  }

  "saveConfirmSector" must {
    "store the sector" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate))
      when(mockBusinessService.getBusiness(any(), any()))
        .thenReturn(Future.successful(validBusiness))
      when(mockConfigConnector.getSicCodeFRSCategory(any())(any()))
        .thenReturn(testBusinessCategory)
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)
      mockReplaceSection[FlatRateScheme](testRegId, incompleteFlatRate.copy(categoryOfBusiness = Some(testBusinessCategory)))

      await(service.saveConfirmSector) mustBe incompleteFlatRate.copy(categoryOfBusiness = Some(testBusinessCategory))
    }

    "store the sector and remove the percentage if there is a sector stored already" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(percent = None))

      await(service.saveConfirmSector) mustBe validFlatRate.copy(percent = None)
    }
  }

  "retrieveBusinessTypeDetails" must {
    val vatBusinessWithNoMainBusinessActivity = validBusiness.copy(mainBusinessActivity = None)

    "retrieve a retrieveSectorPercent if one is saved" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(frs1KReg))
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)

      await(service.retrieveBusinessTypeDetails) mustBe testBusinessTypeDetails
    }

    "determine a retrieveSectorPercent if none is saved but main business activity is known" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate))
      when(mockBusinessService.getBusiness(any(), any()))
        .thenReturn(Future.successful(validBusiness))
      when(mockConfigConnector.getSicCodeFRSCategory(any())(any()))
        .thenReturn(testBusinessCategory)
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)

      await(service.retrieveBusinessTypeDetails) mustBe testBusinessTypeDetails
    }

    "fail if no retrieveSectorPercent is saved and main business activity is not known" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate))
      when(mockBusinessService.getBusiness(any(), any()))
        .thenReturn(Future.successful(vatBusinessWithNoMainBusinessActivity))
      when(mockConfigConnector.getBusinessType(any())(any()))
        .thenReturn(testBusinessTypeDetails)

      intercept[MissingAnswerException](await(service.retrieveBusinessTypeDetails))
    }
  }

  "resetFRSForSAC" must {
    "reset Flat Rate Scheme if the Main Business Activity Sic Code has changed" in new Setup {
      val newSicCode: SicCode = SicCode("newId", "new Desc", "new Details")

      when(mockBusinessService.getBusiness(any(), any()))
        .thenReturn(Future.successful(validBusinessWithNoDescriptionAndLabour))
      mockDeleteSection[FlatRateScheme](testRegId)

      await(service.resetFRSForSAC(newSicCode)) mustBe newSicCode
    }
  }

  "saveStartDate" must {
    "fetch the registration start date and store if answer is VATDate" in new Setup {
      val vatApplication: VatApplication = validVatApplication.copy(startDate = Some(LocalDate.now()))

      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(vatApplication))
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(frsStart = Some(LocalDate.now())))

      await(service.saveStartDate(FRSDateChoice.VATDate, None)) mustBe validFlatRate.copy(frsStart = Some(LocalDate.now()))
    }

    "store the provided date if the answer is DifferentDate" in new Setup {
      mockGetSection[FlatRateScheme](testRegId, Some(validFlatRate))
      mockReplaceSection[FlatRateScheme](testRegId, validFlatRate.copy(frsStart = Some(testDate)))

      await(service.saveStartDate(FRSDateChoice.DifferentDate, Some(testDate))) mustBe validFlatRate.copy(frsStart = Some(testDate))
    }

    "fail if the answer is DifferentDate and there is no date provided" in new Setup {
      intercept[InternalServerException](await(service.saveStartDate(FRSDateChoice.DifferentDate, None)))
    }
  }

  "getPrepopulatedStartDate" must {
    "must get an empty model if there is no saved data" in new Setup() {
      val vatStartDate: LocalDate = LocalDate.now().minusYears(2)

      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(None, None)
    }

    "must get as different date if it does not match the vat start date" in new Setup() {
      val vatStartDate: LocalDate = LocalDate.now().minusYears(2)
      val diffDate: LocalDate = vatStartDate.plusMonths(1)

      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate.copy(frsStart = Some(diffDate))))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(Some(FRSDateChoice.DifferentDate), Some(diffDate))
    }

    "must get vat date if it matches the vat start date" in new Setup() {
      val vatStartDate: LocalDate = LocalDate.now().minusYears(2)

      mockGetSection[FlatRateScheme](testRegId, Some(incompleteFlatRate.copy(frsStart = Some(vatStartDate))))

      await(service.getPrepopulatedStartDate(vatStartDate)) mustBe(Some(FRSDateChoice.VATDate), None)
    }
  }

  "fetchVatStartDate" must {
    "return vat start date when it exists" in new Setup() {
      val vatApplication: VatApplication = validVatApplication.copy(startDate = Some(LocalDate.now()))
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(vatApplication))

      await(service.fetchVatStartDate) mustBe LocalDate.now()
    }

    "return edr when start date does not exist" in new Setup() {
      val vatApplication: VatApplication = validVatApplication.copy(startDate = None)
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.successful(vatApplication))
      when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
        .thenReturn(Future.successful(LocalDate.now()))

      await(service.fetchVatStartDate) mustBe LocalDate.now()
    }

    "return an exception when there is no start date or edr" in new Setup() {
      when(movkVatApplicationService.getVatApplication(any(), any()))
        .thenReturn(Future.failed(new Exception("")))
      when(movkVatApplicationService.retrieveCalculatedStartDate(any(), any()))
        .thenReturn(Future.failed(new Exception("")))

      intercept[Exception](await(service.fetchVatStartDate))
    }
  }

}
