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
import connectors._
import features.returns.models.{Frequency, Returns, Start}
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import models.CurrentProfile
import models.view.Summary
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SummaryControllerSpec extends ControllerSpec with MockMessages with FutureAssertions with VatRegistrationFixture {

  trait Setup {
    val testSummaryController = new SummaryController {
      override val vrs                = mockVatRegistrationService
      override val s4LService         = mockS4LService
      override val keystoreConnector  = mockKeystoreConnector
      override val authConnector      = mockAuthClientConnector
      override val messagesApi        = mockMessagesAPI
      override val summaryService     = mockSummaryService
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val mockVatRegistrationConnector: VatRegistrationConnector = mock[VatRegistrationConnector]

  val fakeRequest = FakeRequest(routes.SummaryController.show())
  override val returns = Returns(Some(true), Some(Frequency.monthly), None, Some(Start(Some(LocalDate.of(2018, 1, 1)))))
  val emptyReturns = Returns(None, None, None, None)

  "Calling summary to show the summary page" should {
    "return HTML with a valid summary view pre-incorp" in new Setup {
      when(mockS4LService.clear(any(), any())) thenReturn Future.successful(validHttpResponse)
      when(mockSummaryService.getRegistrationSummary(any(),any())) thenReturn Future.successful(Summary(Seq.empty))
      when(mockSummaryService.getEligibilityDataSummary(any(),any())) thenReturn Future.successful(fullSummaryModelFromFullEligiblityJson)

      callAuthorised(testSummaryController.show)(_ includesText MOCKED_MESSAGE)
    }

    "return HTML with a valid summary view post-incorp" in new Setup {
      when(mockS4LService.clear(any(),any())) thenReturn Future.successful(validHttpResponse)
      when(mockSummaryService.getRegistrationSummary(any(),any())) thenReturn Future.successful(Summary(Seq.empty))
      when(mockSummaryService.getEligibilityDataSummary(any(),any())) thenReturn Future.successful(fullSummaryModelFromFullEligiblityJson)

      callAuthorised(testSummaryController.show)(_ includesText MOCKED_MESSAGE)
    }
  }

  "Calling submitRegistration" should {
    "redirect to the confirmation page if the status of the document is in draft" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(s"/register-for-vat/submission-confirmation")
      }
    }

    "redirect to the Submission Failed Retryable page when Submission Fails but is Retryable" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any ()))
        .thenReturn(Future.successful(SubmissionFailedRetryable))

      when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.routes.ErrorController.submissionRetryable().url

      }
    }

    "redirect to the Submission Failed page when Submission Fails" in new Setup {
      when(mockVatRegistrationService.getStatus(any())(any()))
        .thenReturn(Future.successful(VatRegStatus.draft))

      when(mockVatRegistrationService.submitRegistration()(any(), any()))
        .thenReturn(Future.successful(SubmissionFailed))

      when(mockKeystoreConnector.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.routes.ErrorController.submissionFailed().url
      }
    }

    "have an internal server error" when {
      "the document is not draft or locked" in new Setup {
        when(mockVatRegistrationService.getStatus(any())(any()))
          .thenReturn(Future.successful(VatRegStatus.held))

        submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withFormUrlEncodedBody()) {
          (result: Future[Result]) =>
            await(result).header.status mustBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
