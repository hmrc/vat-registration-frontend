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

package services

import java.time.LocalDate

import connectors.{CompanyRegistrationConnector, ConfigConnector, KeystoreConnector, VatRegistrationConnector}
import features.turnoverEstimates.{TurnoverEstimates, TurnoverEstimatesService}
import helpers.VatSpec
import models._
import models.api.VatFlatRateScheme
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class FlatRateServiceSpec extends VatSpec {

  class Setup {
    val service = new FlatRateService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val configConnect: ConfigConnector = mockConfigConnector
      override val turnoverEstimateService : TurnoverEstimatesService = mockTurnoverEstimatesService
      override val vatService : VatRegistrationService = mockVatRegistrationService
    }
  }

  class SetupWithFRSThreshold(threshold: Long) {
    val service = new FlatRateService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val configConnect: ConfigConnector = mockConfigConnector
      override val turnoverEstimateService : TurnoverEstimatesService = mockTurnoverEstimatesService
      override val vatService : VatRegistrationService = mockVatRegistrationService

      override def getFlatRateSchemeThreshold()(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] =
        Future.successful(threshold)
    }
  }

  // TODO old tests - look to change
//  "When this is the first time the user starts a journey and we're persisting to the backend" should {
//    "submitVatFlatRateScheme should process the submission even if VatScheme does not contain VatFlatRateScheme" in new Setup {
//      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
//      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any())).thenReturn(validVatFlatRateScheme.pure)
//      save4laterReturns(S4LFlatRateScheme(
//        joinFrs = Some(JoinFrsView(true)),
//        annualCostsInclusive = Some(AnnualCostsInclusiveView("yes")),
//        annualCostsLimited = Some(AnnualCostsLimitedView("yes")),
//        registerForFrs = Some(RegisterForFrsView(true)),
//        frsStartDate = Some(FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE))
//      ))
//      service.submitVatFlatRateScheme() returns validVatFlatRateScheme
//    }
//
//    "submitVatFlatRateScheme should fail if there's no VatFlatRateScheme in backend or S4L" in new Setup {
//      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
//      save4laterReturnsNothing[S4LFlatRateScheme]()
//
//      service.submitVatFlatRateScheme() failedWith classOf[IllegalStateException]
//    }
//  }

  "businessSectorView" should {

    val s4LFlatRateSchemeNoBusinessSector = validS4LFlatRateScheme.copy(categoryOfBusiness = None)
    val s4LFlatRateSchemeBusinessSectorIsBlank = validS4LFlatRateScheme.copy(categoryOfBusiness = Some(BusinessSectorView("", 1)))

    val s4LVatSicAndComplianceNoMainBusinessActivity = s4LVatSicAndCompliance.copy(mainBusinessActivity = None)

    "retrieve a businessSectorView if one is saved" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      await(service.businessSectorView()) mustBe validBusinessSectorView
    }

    "determine a businessSectorView if none is saved but main business activity is known" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndCompliance))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(service.businessSectorView()) mustBe validBusinessSectorView
    }

    "determine a businessSectorView if business sector is blank but main business activity is known" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(s4LFlatRateSchemeBusinessSectorIsBlank)))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndCompliance))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(service.businessSectorView()) mustBe validBusinessSectorView
    }

    "fail if no BusinessSectorView is saved and main business activity is not known" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(s4LFlatRateSchemeBusinessSectorIsBlank)))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndComplianceNoMainBusinessActivity))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      val exception: IllegalStateException = intercept[IllegalStateException](await(service.businessSectorView()))

      exception.getMessage mustBe "Can't determine main business activity"
    }
  }

  "getFlatRateSchemeThreshold" should {

    "return 2000L when the estimated Vat turnover is 100000 (rounded 2%)" in new Setup {
      val jsonBody = Json.obj("vatTaxable" -> 100000)
      val testUrl = "testUrl"
      val financialsWithEstimateVatTurnoverIs100000: TurnoverEstimates = TurnoverEstimates(vatTaxable = 100000L)

      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(financialsWithEstimateVatTurnoverIs100000)))

      val flatRateThreshold: Long = await(service.getFlatRateSchemeThreshold())
      flatRateThreshold mustBe 2000L

    }

    "return 0L if there is no EstimateVatTurnover in the fetched turnover estimate" in new Setup {
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val flatRateThreshold: Long = await(service.getFlatRateSchemeThreshold())
      flatRateThreshold mustBe 0L
    }
  }

  "saveAnnualCostsInclusive" should {

    "return a S4LFlatRateScheme after saving AnnualCostsInclusiveView to S4L" in new Setup {
      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val expected = S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES)))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveAnnualCostsInclusive(AnnualCostsInclusiveView(AnnualCostsInclusiveView.YES)))

      result mustBe Left(expected)
    }
  }

  "saveAnnualCostsLimited" should {

    "return a S4LFlatRateScheme when AnnualCostsLimitedView.selection is No to S4L" in new Setup {
      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val expected: S4LFlatRateScheme = validS4LFlatRateScheme.copy(annualCostsLimited = Some(AnnualCostsLimitedView(AnnualCostsLimitedView.NO)))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveAnnualCostsLimited(AnnualCostsLimitedView(AnnualCostsLimitedView.NO)))

      result mustBe Left(expected)
    }

    "return a S4LFlatRateScheme when AnnualCostsLimitedView.selection is Yes to S4L" in new Setup {
      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val expected: S4LFlatRateScheme = validS4LFlatRateScheme.copy(
        annualCostsLimited = Some(AnnualCostsLimitedView(AnnualCostsLimitedView.YES)),
        frsStartDate = None,
        categoryOfBusiness = None
      )

      val result: Either[S4LFlatRateScheme, _] = await(service.saveAnnualCostsLimited(AnnualCostsLimitedView(AnnualCostsLimitedView.YES)))

      result mustBe Left(expected)
    }
  }

  "isOverLimitedCostTraderThreshold" should {

    val underThreshold = 500L
    val overThreshold = 2000L

    "return true when the calculated flat rate scheme threshold is over 1000" in new SetupWithFRSThreshold(overThreshold) {
      val result: Boolean = await(service.isOverLimitedCostTraderThreshold)
      result mustBe true
    }

    "return false when the calculated flat rate scheme threshold is below 1000" in new SetupWithFRSThreshold(underThreshold) {
      val result: Boolean = await(service.isOverLimitedCostTraderThreshold)
      result mustBe false
    }
  }

  "frsViewToApi" should {

    "transform a S4LFlatRateScheme into a VatFlatRateScheme" in new Setup {
      service.frsViewToApi(validS4LFlatRateScheme) mustBe validVatFlatRateScheme
    }
  }

  "frsApiToView" should {

    "transform a VatScheme into a S4LFlatRateScheme if a VatFlatRateScheme exists" in new Setup {
      service.frsApiToView(validVatScheme) mustBe validS4LFlatRateScheme
    }

    "return an empty S4LFlatRateScheme if the supplied VatScheme does not contain a VatFlatRateScheme" in new Setup {
      service.frsApiToView(emptyVatScheme) mustBe S4LFlatRateScheme()
    }
  }

  "fetchFlatRateScheme" should {

    "return a S4LFlatRateScheme if one is found in Save 4 Later" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe validS4LFlatRateScheme
    }

    "return a S4LFlatRateScheme if nothing is found in Save 4 Later but one is found in Vat Registration" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe validS4LFlatRateScheme
    }

    "return an empty S4LFlatRateScheme if nothing is found in either Save 4 Later or Vat Registration" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe S4LFlatRateScheme()
    }
  }

  "fetchFRSFromS4L" should {

    "return a S4LFlatRateScheme if one is found in Save 4 Later" in new Setup {

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val result: Option[S4LFlatRateScheme] = await(service.fetchFRSFromS4L)

      result mustBe Some(validS4LFlatRateScheme)
    }

    "return None if a S4LFlatRateScheme is not found in Save 4 Later" in new Setup {

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result: Option[S4LFlatRateScheme] = await(service.fetchFRSFromS4L)

      result mustBe None
    }
  }

  "fetchFRSFromAPI" should {

    "return a VatFlatRateScheme if a VatScheme is found containing one" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      val result: Option[VatFlatRateScheme] = await(service.fetchFRSFromAPI)

      result mustBe Some(validVatFlatRateScheme)
    }

    "return None when a VatFlatRateScheme is not found in the returned VatScheme" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      val result: Option[VatFlatRateScheme] = await(service.fetchFRSFromAPI)

      result mustBe None
    }
  }

  "saveRegisterForFRS" should {
    "save not registered for FRS" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveRegisterForFRS(registerForFrs = false, Some(validBusinessSectorView)))

      result mustBe Left(validS4LFlatRateScheme.copy(registerForFrs = None, frsStartDate = None))
    }

    "save registered for FRS" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveRegisterForFRS(registerForFrs = true, Some(validBusinessSectorView)))

      result mustBe Left(validS4LFlatRateScheme.copy(registerForFrs = Some(RegisterForFrsView(true))))
    }
  }

  "submitVatFlatRateScheme" should {
    "save the vat flat rate scheme" in new Setup {
      when(mockVatRegistrationService.s4l[S4LFlatRateScheme](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatFlatRateScheme))

      val result = await(service.submitVatFlatRateScheme)

      result mustBe validVatFlatRateScheme
    }
  }

  "saveJoinFRS" should {

    "save JoinFrsView To S4L if the logical block is incomplete" in new Setup {
      // TODO this function can only save to S4L at the moment
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveJoinFRS(JoinFrsView(true)))

      result mustBe Left(validS4LFlatRateScheme)
    }

    "save a FlatRateScheme directly to Vat Registration if JoinFrsView.selection is false" in new Setup {

      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any()))
        .thenReturn(Future.successful(vatFlatRateSchemeNotJoiningFRS))

      val result: Either[_, VatFlatRateScheme] = await(service.saveJoinFRS(JoinFrsView(false)))

      result mustBe Right(vatFlatRateSchemeNotJoiningFRS)
    }
  }

  "saveFRSStartDate" should {

    "save FRS Start Date if it is the vat registration date" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val startDateView = FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE, Some(LocalDate.of(2017, 10, 10)))
      val result: Either[S4LFlatRateScheme, _] = await(service.saveFRSStartDate(
        FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE)
      ))

      result mustBe Left(validS4LFlatRateScheme.copy(frsStartDate = Some(startDateView)))
    }

    "save FRS Start Date which is a different date" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val startDateView = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(LocalDate.now()))
      val result: Either[S4LFlatRateScheme, _] = await(service.saveFRSStartDate(
        startDateView
      ))

      result mustBe Left(validS4LFlatRateScheme.copy(frsStartDate = Some(startDateView)))
    }
  }

  "saveBusinessSector" should {
    "save a business sector in category of business" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val businessSectorView = BusinessSectorView("test", BigDecimal(15))
      val result: Either[S4LFlatRateScheme, _] = await(service.saveBusinessSector(
        businessSectorView
      ))

      result mustBe Left(validS4LFlatRateScheme.copy(categoryOfBusiness = Some(businessSectorView)))
    }
  }

  "saveFRS" should {

    "save to S4L" in new Setup {

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveFRS(validS4LFlatRateScheme))

      result mustBe Left(validS4LFlatRateScheme)
    }
  }

  "saveFRSToS4L" should {

    "return a VatFlatRateScheme that has been saved in Vat Registration" in new Setup {

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: S4LFlatRateScheme = await(service.saveFRSToS4L(validS4LFlatRateScheme))

      result mustBe validS4LFlatRateScheme
    }
  }

  "saveFRStoAPI" should {

    "return a VatFlatRateScheme that has been saved in Vat Registration" in new Setup {

      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatFlatRateScheme))

      val result: VatFlatRateScheme = await(service.saveFRStoAPI(validVatFlatRateScheme))

      result mustBe validVatFlatRateScheme
    }
  }


}
