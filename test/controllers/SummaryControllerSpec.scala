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

package controllers

import java.time.LocalDate

import common.enums.VatRegStatus
import connectors.{Success, VatRegistrationConnector}
import features.returns.{Frequency, Returns, Start}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.ModelKeys.INCORPORATION_STATUS
import models.external.IncorporationInfo
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Matchers
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.Future

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestSummaryController extends SummaryController(
    ds,
    mockVATFeatureSwitch,
    mockVatRegistrationService,
    mockReturnsService,
    mockKeystoreConnector,
    mockAuthConnector,
    mockLodgingOfficerService,
    mockS4LService
  )

  val mockVatRegistrationConnector: VatRegistrationConnector = mock[VatRegistrationConnector]

  val fakeRequest = FakeRequest(routes.SummaryController.show())
  val returns = Returns(Some(true), Some(Frequency.monthly), None, Some(Start(Some(LocalDate.of(2018, 1, 1)))))
  val emptyReturns = Returns(None, None, None, None)

  "Calling summary to show the summary page" should {

    "return HTML with a valid summary view pre-incorp" in {
      when(mockS4LService.clear(any(), any())).thenReturn(Future.successful(validHttpResponse))
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()
      when(mockLodgingOfficerService.getLodgingOfficer(any(),any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "return HTML with a valid summary view post-incorp" in {
      when(mockS4LService.clear(any(),any())).thenReturn(Future.successful(validHttpResponse))
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, None)
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()
      when(mockLodgingOfficerService.getLodgingOfficer(any(),any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "getRegistrationSummary maps a valid VatScheme object to a Summary object" in {
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(Future.successful(validVatScheme))
      implicit val cp = currentProfile()
      when(mockLodgingOfficerService.getLodgingOfficer(any(),any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      TestSummaryController.getRegistrationSummary().map(summary => summary.sections.length mustEqual 2)
    }

    "registrationToSummary maps a valid VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(validVatScheme).sections.length mustEqual 11
    }

    "registrationToSummary maps a valid empty VatScheme object to a Summary object" in {
      when(mockLodgingOfficerService.getLodgingOfficer(any(),any()))
        .thenReturn(Future.successful(validFullLodgingOfficer))
      TestSummaryController.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency).sections.length mustEqual 11
    }
  }

  "Calling submitRegistration" should {
    "redirect to the confirmation page if the status of the document is in draft" in {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      mockGetCurrentProfile()

      when(mockVatRegistrationService.submitRegistration()(any(), any()))
        .thenReturn(Future.successful(Success))

      submitAuthorised(TestSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
      }
    }

    "have an internal server error if the document is not in draft" in {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.acknowledged))

      mockGetCurrentProfile()

      when(mockVatRegistrationService.submitRegistration()(any(), any()))
        .thenReturn(Future.successful(Success))

      submitAuthorised(TestSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}
