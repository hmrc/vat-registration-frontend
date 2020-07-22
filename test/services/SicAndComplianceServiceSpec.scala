/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.RegistrationConnector
import fixtures.VatRegistrationFixture
import mocks.VatMocks
import models.api.SicCode
import models._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SicAndComplianceServiceSpec extends UnitSpec with MockitoSugar with VatMocks with VatRegistrationFixture {
  implicit val hc = HeaderCarrier()
  implicit val currentProfile = CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft, None)

  trait Setup {
    val service: SicAndComplianceService = new SicAndComplianceService {
      override val registrationConnector: RegistrationConnector = mockRegConnector
      override val s4lService: S4LService = mockS4LService
      override val vrs: VatRegistrationService = mockVatRegistrationService
    }
  }

  "getSicAndCompliance" should {
    "return a Sic And Compliance view model" when {
      "there is data in S4L" in new Setup {
        when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(s4lVatSicAndComplianceWithLabour)))

        await(service.getSicAndCompliance) shouldBe s4lVatSicAndComplianceWithLabour
      }

      "there is no data in S4L but present in backend" in new Setup {
        when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(s4lVatSicAndComplianceWithLabour)))

        await(service.getSicAndCompliance) shouldBe s4lVatSicAndComplianceWithLabour
      }
    }
    "return an empty Sic And Compiance view model" when {

      "there is no data in S4L or vat reg" in new Setup {
        when(mockRegConnector.getSicAndCompliance(any(),any())).thenReturn(Future.successful(None))
        when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))
        when(mockS4LService.saveNoAux[SicAndCompliance](any(),any())(any(),any(),any())).thenReturn(Future.successful(validCacheMap))

        await(service.getSicAndCompliance) shouldBe SicAndCompliance()
      }
    }
  }

  "updateSicAndCompliance" should {
    "update S4l and not vat reg because the model is incomplete and return the sicAndCompliance" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(SicAndCompliance())))
      when(mockS4LService.saveNoAux[SicAndCompliance](any(), any())(any(), any(), any())).thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(Workers(200))) shouldBe SicAndCompliance(workers = Some(Workers(200)))
    }

    "update S4l and not vat reg because the model is incomplete without SIC code and return the sicAndCompliance" in new Setup {
      val incompleteSicAndCompliance = SicAndCompliance(
        description = Some(BusinessActivityDescription("foo")),
        mainBusinessActivity = None
      )
      val data = MainBusinessActivityView("foo", None)
      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteSicAndCompliance)))
      when(mockS4LService.saveNoAux[SicAndCompliance](any(), any())(any(), any(), any())).thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) shouldBe incompleteSicAndCompliance.copy(mainBusinessActivity = Some(data))
    }

    "update S4l and not vat reg because the model is incomplete without skilled workers and return the sicAndCompliance" in new Setup {
      val incompleteSicAndCompliance = SicAndCompliance(
        description = Some(BusinessActivityDescription("foo")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo", Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = None
      )
      val data = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteSicAndCompliance)))
      when(mockS4LService.saveNoAux[SicAndCompliance](any(), any())(any(), any(), any())).thenReturn(Future.successful(validCacheMap))

      await(service.updateSicAndCompliance(data)) shouldBe incompleteSicAndCompliance.copy(temporaryContracts = Some(data))
    }

    "update vat reg (not s4l) and clear S4l when model is complete without labour" in new Setup {
      val data = MainBusinessActivityView("foo",Some(sicCode))
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = None
      )
      val expected = incompleteViewModel.copy(mainBusinessActivity = Some(data))

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.updateSicAndCompliance(data)) shouldBe expected
    }

    "update vat reg (not s4l) and clear S4l when model is complete with labour but no temporary workers" in new Setup {
      val data = TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_NO)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8))
      )
      val expected = incompleteViewModel.copy(temporaryContracts = Some(data))

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.updateSicAndCompliance(data)) shouldBe expected
    }

    "update vat reg (not s4l) and clear S4l when model is complete with labour but no workers" in new Setup {
      val data = CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(sicCode)))
      )
      val expected = incompleteViewModel.copy(companyProvideWorkers = Some(data))

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.updateSicAndCompliance(data)) shouldBe expected
    }

    "update vat reg (not s4l) and clear S4l when model is complete with workers less than 8" in new Setup {
      val data = Workers(7)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = None
      )
      val expected = incompleteViewModel.copy(workers = Some(data))

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.updateSicAndCompliance(data)) shouldBe expected
    }

    "update vat reg (not s4l) and clear S4l when model is complete with labour with workers" in new Setup {
      val data = SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES)
      val incompleteViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = None
      )
      val expected = incompleteViewModel.copy(skilledWorkers = Some(data))

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incompleteViewModel)))

      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.updateSicAndCompliance(data)) shouldBe expected
    }
  }

  "submitSicCodes" should {
    "return a view model with Labour Compliance removed when none SIC Code Labour is provided" in new Setup {
      val code = SicCode("88888", "description", "displayDetails")
      val sicCodeList = List(SicCode("66666", "test1", "desc1"), code)
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(code))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
      )
      val expected = completeViewModel.copy(mainBusinessActivity = None, otherBusinessActivities = Some(OtherBusinessActivities(sicCodeList)),
        companyProvideWorkers = None, workers = None, temporaryContracts = None, skilledWorkers = None
      )

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.submitSicCodes(sicCodeList)) shouldBe expected
    }

    "return a view model with Main Business Activity updated when only one Labour SIC Code is provided" in new Setup {
      val newSicCode = SicCode("01610", "test1", "desc1")
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
      )
      val expected = completeViewModel.copy(
         mainBusinessActivity = Some(MainBusinessActivityView(newSicCode)),
         otherBusinessActivities = Some(OtherBusinessActivities(List(newSicCode)))
      )

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.submitSicCodes(List(newSicCode))) shouldBe expected
    }

    "return a view model with Main Business Activity updated & Labour Compliance removed when only one none Labour SIC Code is provided" in new Setup {
      val newSicCode = SicCode("666666", "test1", "desc1")
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView(sicCode.code, Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
      )
      val expected = completeViewModel.copy(
        otherBusinessActivities = Some(OtherBusinessActivities(List(newSicCode))),
        mainBusinessActivity = Some(MainBusinessActivityView(newSicCode)),
        companyProvideWorkers = None,
        workers = None,
        temporaryContracts = None,
        skilledWorkers = None)

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.submitSicCodes(List(newSicCode))) shouldBe expected
    }

    "return a view model without Labour Compliance being removed when SIC Code Labour is provided" in new Setup {
      val sicCodeList = List(SicCode("01610", "test1", "desc1"), SicCode("81223", "test2", "desc2"))
      val completeViewModel = SicAndCompliance(
        description = Some(BusinessActivityDescription("foobar")),
        mainBusinessActivity = Some(MainBusinessActivityView("foo",Some(sicCode))),
        companyProvideWorkers = Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES)),
        workers = Some(Workers(8)),
        temporaryContracts = Some(TemporaryContracts(TemporaryContracts.TEMP_CONTRACTS_YES)),
        skilledWorkers = Some(SkilledWorkers(SkilledWorkers.SKILLED_WORKERS_YES))
      )

      when(mockS4LService.fetchAndGetNoAux[SicAndCompliance](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(completeViewModel)))
      when(mockRegConnector.updateSicAndCompliance(any())(any(),any())).thenReturn(Future.successful(Json.obj()))
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))

      await(service.submitSicCodes(sicCodeList)) shouldBe completeViewModel.copy(mainBusinessActivity = None, otherBusinessActivities = Some(OtherBusinessActivities(sicCodeList)))
    }
  }
}
