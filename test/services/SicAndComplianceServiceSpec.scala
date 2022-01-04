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

import common.enums.VatRegStatus
import models._
import models.api.SicCode
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.Json
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SicAndComplianceServiceSpec extends VatRegSpec {
  override implicit val hc: HeaderCarrier = HeaderCarrier()
  override implicit val currentProfile: CurrentProfile = CurrentProfile(testRegId, VatRegStatus.draft)

  trait Setup {
    val service: SicAndComplianceService = new SicAndComplianceService(
      mockS4LService,
      mockVatRegistrationService,
      mockVatRegistrationConnector
    )
  }

  "getSicAndCompliance" should {
    "return a Sic And Compliance view model" when {
      "there is data in S4L" in new Setup {
        when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(s4lVatSicAndComplianceWithLabour)))

        await(service.getSicAndCompliance) mustBe s4lVatSicAndComplianceWithLabour
      }
      "there is no data in S4L but present in backend" in new Setup {
        when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(s4lVatSicAndComplianceWithLabour)))

        await(service.getSicAndCompliance) mustBe s4lVatSicAndComplianceWithLabour
      }
    }
    "return an empty Sic And Compiance view model" when {
      "there is no data in S4L or vat reg" in new Setup {
        when(mockVatRegistrationConnector.getSicAndCompliance(any(), any())).thenReturn(Future.successful(None))
        when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        when(mockS4LService.save[SicAndCompliance](any())(any(), any(), any(), any())).thenReturn(Future.successful(validCacheMap))

        await(service.getSicAndCompliance) mustBe SicAndCompliance()
      }
    }
  }

  "updateSicAndCompliance" should {
    "update S4l and not vat reg because the model is incomplete and return the sicAndCompliance" in new Setup {
      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(SicAndCompliance())))
      when(mockVatRegistrationConnector.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(None))
      when(mockS4LService.save[SicAndCompliance](any())(any(), any(), any(), any())).thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(Workers(200))) mustBe SicAndCompliance(workers = Some(Workers(200)))
    }
    "update S4l and not vat reg because the model is incomplete without SIC code and return the sicAndCompliance" in new Setup {
      val incompleteSicAndCompliance = SicAndCompliance(
        description = Some(BusinessActivityDescription("foo")),
        mainBusinessActivity = None
      )
      val data = MainBusinessActivityView("foo", None)
      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteSicAndCompliance)))
      when(mockS4LService.save[SicAndCompliance](any())(any(), any(), any(), any())).thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) mustBe incompleteSicAndCompliance.copy(mainBusinessActivity = Some(data))
    }
    "update vat reg (not s4l) and clear S4l when model is complete without labour" in new Setup {
      val data = MainBusinessActivityView("foo", Some(sicCode))
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = None
      )
      val expected = incompleteViewModel.copy(mainBusinessActivity = Some(data))

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) mustBe expected
    }
    "update vat reg (not s4l) and clear S4l when model is complete with labour but no temporary workers" in new Setup {
      val data = IntermediarySupply(false)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(sicCode))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = Some(Workers(8))
      )
      val expected = incompleteViewModel.copy(intermediarySupply = Some(data))

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) mustBe expected
    }
    "update vat reg (not s4l) and clear S4l when model is complete with labour but no workers" in new Setup {
      val data = SupplyWorkers(false)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(sicCode)))
      )
      val expected = incompleteViewModel.copy(supplyWorkers = Some(data), workers = None)

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) mustBe expected
    }
    "update vat reg (not s4l) and clear S4l when model is complete with workers less than 8" in new Setup {
      val data = Workers(7)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(sicCode))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = None
      )
      val expected = incompleteViewModel.copy(workers = Some(data))

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) mustBe expected
    }
  }

  "submitSicCodes" should {
    "return a view model with Labour Compliance removed when none SIC Code Labour is provided" in new Setup {
      val code = SicCode("88888", "description", "displayDetails")
      val sicCodeList = List(SicCode("66666", "test1", "desc1"), code)
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(code))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = Some(Workers(8)),
        intermediarySupply = Some(IntermediarySupply(true))
      )
      val expected = completeViewModel.copy(mainBusinessActivity = None, businessActivities = Some(BusinessActivities(sicCodeList)),
        supplyWorkers = None, workers = None, intermediarySupply = None
      )

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.submitSicCodes(sicCodeList)) mustBe expected
    }
    "return a view model with Main Business Activity updated when only one Labour SIC Code is provided" in new Setup {
      val newSicCode = SicCode("01610", "test1", "desc1")
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = Some(Workers(8)),
        intermediarySupply = Some(IntermediarySupply(true))
      )
      val expected = completeViewModel.copy(
        mainBusinessActivity = Some(MainBusinessActivityView(newSicCode)),
        businessActivities = Some(BusinessActivities(List(newSicCode)))
      )

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.submitSicCodes(List(newSicCode))) mustBe expected
    }
    "return a view model with Main Business Activity updated & Labour Compliance removed when only one none Labour SIC Code is provided" in new Setup {
      val newSicCode = SicCode("666666", "test1", "desc1")
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = Some(Workers(8)),
        intermediarySupply = Some(IntermediarySupply(true))
      )
      val expected = completeViewModel.copy(
        businessActivities = Some(BusinessActivities(List(newSicCode))),
        mainBusinessActivity = Some(MainBusinessActivityView(newSicCode)),
        supplyWorkers = None,
        workers = None,
        intermediarySupply = None
      )
      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.submitSicCodes(List(newSicCode))) mustBe expected
    }
    "return a view model without Labour Compliance being removed when SIC Code Labour is provided" in new Setup {
      val sicCodeList = List(SicCode("01610", "test1", "desc1"), SicCode("81223", "test2", "desc2"))
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(sicCode))),
        supplyWorkers = Some(SupplyWorkers(true)),
        workers = Some(Workers(8)),
        intermediarySupply = Some(IntermediarySupply(true))
      )

      when(mockS4LService.fetchAndGet[SicAndCompliance](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))

      when(mockS4LService.save(any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      when(mockVatRegistrationConnector.updateSicAndCompliance(any())(any(), any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clearKey(any(), any(), any()))
        .thenReturn(Future.successful(validCacheMap))

      await(service.submitSicCodes(sicCodeList)) mustBe completeViewModel.copy(mainBusinessActivity = None, businessActivities = Some(BusinessActivities(sicCodeList)))
    }
  }
}
