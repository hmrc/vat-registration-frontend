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

package controllers

import common.enums.VatRegStatus
import connectors.NonRepudiationConnector.StoreNrsPayloadSuccess
import connectors._
import featureswitch.core.config.FeatureSwitching
import fixtures.VatRegistrationFixture
import models.CurrentProfile
import models.api.returns.{Monthly, Returns}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import services.mocks.MockNonRepudiationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.pages.Summary

import java.time.LocalDate
import scala.concurrent.Future

class SummaryControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with MockNonRepudiationService with FeatureSwitching {

  trait Setup {
    val testSummaryController = new SummaryController(
      mockSessionService,
      mockAuthClientConnector,
      mockVatRegistrationService,
      mockS4LService,
      mockSummaryService,
      mockNonRepuidiationService,
      app.injector.instanceOf[Summary]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.SummaryController.show)
  override val returns: Returns = Returns(Some(10000.5), Some(true), Some(Monthly), None, Some(LocalDate.of(2018, 1, 1)))
  val emptyReturns: Returns = Returns()

  "Calling summary to show the summary page" when {
    "the StoreAnswersForNrs feature switch is enabled" should {
      "return OK with a valid summary view" in new Setup {
        when(mockS4LService.clear(any(), any())) thenReturn Future.successful(validHttpResponse)
        when(mockSummaryService.getRegistrationSummary(any(), any(), any()))
          .thenReturn(Future.successful(SummaryList()))
        when(mockSummaryService.getEligibilityDataSummary(any(), any(), any()))
          .thenReturn(Future.successful(SummaryList()))
        mockStoreEncodedUserAnswers(regId)(Future.successful(StoreNrsPayloadSuccess))

        callAuthorised(testSummaryController.show)(status(_) mustBe OK)
      }
    }
  }

  "Calling submitRegistration" should {
    "redirect to the confirmation page if the status of the document is in draft" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody(), useBasicAuth = true) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "redirect to the Submission Failed Retryable page when Submission Fails but is Retryable" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any()))
        .thenReturn(Future.successful(SubmissionFailedRetryable))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody(), useBasicAuth = true) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.routes.ErrorController.submissionRetryable.url

      }
    }

    "redirect to the Submission Failed page when Submission Fails" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any()))
        .thenReturn(Future.successful(SubmissionFailed))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody(), useBasicAuth = true) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.routes.ErrorController.submissionFailed.url
      }
    }
  }
}