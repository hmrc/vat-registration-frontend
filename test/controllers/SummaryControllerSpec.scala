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

import common.enums.VatRegStatus
import connectors.{KeystoreConnector, Success, VatRegistrationConnector}
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CurrentProfile
import models.ModelKeys.INCORPORATION_STATUS
import models.external.IncorporationInfo
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.redirectLocation
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestSummaryController extends SummaryController(
    ds,
    mockVATFeatureSwitch,
    mockVatRegistrationService,
    mockKeystoreConnector,
    mockAuthConnector,
    mockS4LService
  )

  val mockVatRegistrationConnector: VatRegistrationConnector = mock[VatRegistrationConnector]

  val fakeRequest = FakeRequest(routes.SummaryController.show())

  "Calling summary to show the summary page" should {

    "return HTML with a valid summary view pre-incorp" in {
      when(mockS4LService.clear(any(), any())).thenReturn(validHttpResponse.pure)
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, Some(testIncorporationInfo))
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(Future.successful(validVatScheme))
      mockGetCurrentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "return HTML with a valid summary view post-incorp" in {
      when(mockS4LService.clear(any(),any())).thenReturn(validHttpResponse.pure)
      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, None)
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(validVatScheme.pure)
      mockGetCurrentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "getRegistrationSummary maps a valid VatScheme object to a Summary object" in {
      when(mockVatRegistrationService.getVatScheme(any(),any())).thenReturn(validVatScheme.pure)
      implicit val cp = currentProfile()
      when(mockVATFeatureSwitch.disableEligibilityFrontend).thenReturn(enabledFeatureSwitch)
      TestSummaryController.getRegistrationSummary().map(summary => summary.sections.length mustEqual 2)
    }

    "registrationToSummary maps a valid VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(validVatScheme).sections.length mustEqual 10
    }

    "registrationToSummary maps a valid empty VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency).sections.length mustEqual 10
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
