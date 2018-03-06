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

import connectors.{ConfigConnector, VatRegistrationConnector}
import features.returns.models.Start
import features.sicAndCompliance.services.SicAndComplianceService
import features.turnoverEstimates.{TurnoverEstimates, TurnoverEstimatesService}
import frs.{AnnualCosts, FRSDateChoice, FlatRateScheme}
import helpers.VatSpec
import models._
import models.api.SicCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class FlatRateServiceSpec extends VatSpec {

  class Setup {
    val service = new FlatRateService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val configConnector: ConfigConnector = mockConfigConnector
      override val turnoverEstimateService : TurnoverEstimatesService = mockTurnoverEstimatesService
      override val sicAndComplianceService: SicAndComplianceService = mockSicAndComplianceService
    }
  }

  class SetupWithFRSThreshold(threshold: Long) {
    val service = new FlatRateService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val configConnector: ConfigConnector = mockConfigConnector
      override val turnoverEstimateService : TurnoverEstimatesService = mockTurnoverEstimatesService
      override val sicAndComplianceService: SicAndComplianceService = mockSicAndComplianceService

      override def getFlatRateSchemeThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] =
        Future.successful(threshold)
    }
  }

  val financialsWithEstimateVatTurnoverIs100000: TurnoverEstimates = TurnoverEstimates(vatTaxable = 100000L)

  "getFlatRateSchemeThreshold" should {

    "return 2000L when the estimated Vat turnover is 100000 (rounded 2%)" in new Setup {
      val jsonBody = Json.obj("vatTaxable" -> 100000)
      val testUrl = "testUrl"

      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(financialsWithEstimateVatTurnoverIs100000)))

      val flatRateThreshold: Long = await(service.getFlatRateSchemeThreshold)
      flatRateThreshold mustBe 2000L

    }

    "return 0L if there is no EstimateVatTurnover in the fetched turnover estimate" in new Setup {
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val flatRateThreshold: Long = await(service.getFlatRateSchemeThreshold)
      flatRateThreshold mustBe 0L
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

  "fetchVatStartDate" should {
    "return the vat start date present in the returns" in new Setup {
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validReturns))

      await(service.fetchVatStartDate) mustBe validReturns.start.get.date
    }

    "return None when there was a problem retrieving the vat start date" in new Setup {
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.failed(new Exception))

      await(service.fetchVatStartDate) mustBe None
    }
  }

  /*
   private[services] def fetchVatStartDate(implicit headerCarrier: HeaderCarrier, currentProfile: CurrentProfile) : Future[Option[LocalDate]] = {
    vatRegConnector.getReturns(currentProfile.registrationId) map {returns =>
      returns.start.flatMap(_.date)
    } recover {
      case e => None
    }
  }
   */



  "getFlatRate" should {
    val frSch = FlatRateScheme(
      Some(false)
    )

    "return the S4L model if it is there" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(frSch)))

      await(service.getFlatRate) mustBe frSch
    }

    "return the converted backend model if the S4L is not there" in new Setup() {


      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockRegConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(Some(frSch)))

      await(service.getFlatRate) mustBe frSch
    }

    "return an empty backend model if the S4L is not there and neither is the backend" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockRegConnector.getFlatRate(any())(any()))
        .thenReturn(Future.successful(None))

      await(service.getFlatRate) mustBe FlatRateScheme()
    }
  }

  "handleView" should {
    "convert a S4L model (over 1000, registers)" in new Setup() {
      service.handleView(frs1KReg) mustBe Complete(frs1KReg)
    }

    "convert a S4L model (over 1000, not register)" in new Setup() {
      service.handleView(frs1KNreg) mustBe Complete(frs1KNreg)
    }

    "convert a S4L model (over %, registers)" in new Setup() {
      service.handleView(frsPerReg) mustBe Complete(frsPerReg)
    }

    "convert a S4L model (skip %, confirms)" in new Setup() {
      service.handleView(frsPerNconf) mustBe Complete(frsPerNconf)
    }

    "convert a S4L model (skip %, does not confirm)" in new Setup() {
      service.handleView(frsPerNconfN) mustBe Complete(frsPerNconfN)
    }

    "convert a S4L model (does not join)" in new Setup() {
      service.handleView(frsNoJoin) mustBe Complete(frsNoJoin)
    }

    "do not handle an incomplete" in new Setup() {
      service.handleView(incompleteS4l) mustBe Incomplete(incompleteS4l)
    }

    "do not handle an empty S4L" in new Setup() {
      service.handleView(FlatRateScheme.empty) mustBe Incomplete(FlatRateScheme.empty)
    }
  }

  "submitFlatRate" should {
    "if the S4L model is incomplete, save to S4L" in new Setup() {
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.submitFlatRate(incompleteS4l)) mustBe incompleteS4l
    }

    "if the S4L model is complete, save to the backend and clear S4L" in new Setup() {
      when(mockRegConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.submitFlatRate(frs1KReg)) mustBe frs1KReg
    }
  }

  "saveJoiningFRS" should {
    "save joining the FRS" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(FlatRateScheme.empty)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveJoiningFRS(answer = true)) mustBe FlatRateScheme(joinFrs = Some(true))
    }

    "save not joining the FRS (and save to backend)" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(FlatRateScheme.empty)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockRegConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.saveJoiningFRS(answer = false)) mustBe FlatRateScheme(joinFrs = Some(false))
    }
  }

  "saveOverAnnualCosts" should {
    "save that they have gone over" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(financialsWithEstimateVatTurnoverIs100000)))

      await(service.saveOverAnnualCosts(AnnualCosts.AlreadyDoesSpend)) mustBe
        incompleteS4l.copy(overBusinessGoods = Some(AnnualCosts.AlreadyDoesSpend))
    }

    "save that they do not go over" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(financialsWithEstimateVatTurnoverIs100000)))

      await(service.saveOverAnnualCosts(AnnualCosts.DoesNotSpend)) mustBe
        incompleteS4l.copy(overBusinessGoods = Some(AnnualCosts.DoesNotSpend), vatTaxableTurnover = Some(2000L))
    }
  }

  "saveOverAnnualCostsPercent" should {
    "save that they have gone over" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverAnnualCostsPercent(AnnualCosts.AlreadyDoesSpend)) mustBe
        incompleteS4l.copy(overBusinessGoodsPercent = Some(AnnualCosts.AlreadyDoesSpend))
    }

    "save that they do not go over" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveOverAnnualCostsPercent(AnnualCosts.DoesNotSpend)) mustBe
        incompleteS4l.copy(overBusinessGoodsPercent = Some(AnnualCosts.DoesNotSpend))
    }
  }

  "saveRegister" should {
    "save that they want to register" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveRegister(answer = true)) mustBe
        incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some(""), percent = Some(defaultFlatRate))
    }

    "save that they do not wish to register (clearing the start date, saving to the backend)" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(frsStart =
          Some(Start(Some(LocalDate.of(2017, 10, 10))))
        ))))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockRegConnector.upsertFlatRate(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(202)))

      await(service.saveRegister(answer = false)) mustBe
        incompleteS4l.copy(useThisRate = Some(false), categoryOfBusiness = Some(""), percent = Some(defaultFlatRate), frsStart = None)
    }
  }

  "retrieveSectorPercent" should {
    val s4LVatSicAndComplianceNoMainBusinessActivity = s4lVatSicAndComplianceWithLabour.copy(mainBusinessActivity = None)

    "retrieve a retrieveSectorPercent if one is saved" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(frs1KReg)))

      await(service.retrieveSectorPercent) mustBe ("test", defaultFlatRate)
    }

    "determine a retrieveSectorPercent if none is saved but main business activity is known" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(service.retrieveSectorPercent) mustBe (validBusinessSectorView._1, validBusinessSectorView._2)
    }

    "fail if no retrieveSectorPercent is saved and main business activity is not known" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndComplianceNoMainBusinessActivity))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      val exception: IllegalStateException = intercept[IllegalStateException](await(service.retrieveSectorPercent))

      exception.getMessage mustBe "[FlatRateService] [retrieveSectorPercent] Can't determine main business activity"
    }
  }

  "saveUseFlatRate" should {
    "save that they want to use the flat rate" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate)))))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveUseFlatRate(answer = true)) mustBe
        incompleteS4l.copy(useThisRate = Some(true), categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate))
    }
  }

  "getPrepopulatedStartDate" should {
    "should get an empty model if there is nothing in S4L" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      await(service.getPrepopulatedStartDate) mustBe (None, None)
    }

    "should get as different date if it does not match the vat start date" in new Setup() {
      val diffdate = LocalDate.of(2017, 11, 11)

      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(
          Some(incompleteS4l.copy(frsStart = Some(Start(Some(diffdate)))))
        ))
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      await(service.getPrepopulatedStartDate) mustBe (Some(FRSDateChoice.DifferentDate), Some(diffdate))
    }

    "should get vat date if it matches the vat start date" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(
          Some(incompleteS4l.copy(frsStart = Some(Start(None))))
        ))
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      await(service.getPrepopulatedStartDate) mustBe (Some(FRSDateChoice.VATDate), None)
    }
  }

  "saveConfirmSector" should {
    "save their confirmation of a sector" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l.copy(categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate)))))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveConfirmSector) mustBe
        incompleteS4l.copy(categoryOfBusiness = Some("test"), percent = Some(defaultFlatRate))
    }
  }

  "saveStartDate" should {
    "save that the start date should be the vat start date" in new Setup() {
      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme.returns.get))

      await(service.saveStartDate(FRSDateChoice.VATDate, None)) mustBe
        incompleteS4l.copy(frsStart = Some(Start(validVatScheme.returns.get.start.get.date)))
    }

    "save that the start date should be a different date" in new Setup() {
      val frsStart = LocalDate.of(2017, 10, 10)

      when(mockS4LService.fetchAndGetNoAux[FlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteS4l)))
      when(mockS4LService.saveNoAux(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      await(service.saveStartDate(FRSDateChoice.DifferentDate, Some(frsStart))) mustBe
        incompleteS4l.copy(frsStart = frsDate)
    }
  }

  "resetFRS" should {
    "reset Flat Rate Scheme if the Main Business Activity Sic Code has changed" in new Setup {
      val newSicCode = SicCode("newId", "new Desc", "new Details")

      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      when(mockS4LService.saveNoAux[FlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))
      when(mockRegConnector.clearFlatRate(any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.resetFRS(newSicCode)) mustBe newSicCode
    }
  }
}
