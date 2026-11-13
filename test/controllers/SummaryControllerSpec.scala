/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors._
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import fixtures.VatRegistrationFixture
import models.CurrentProfile
import models.api.{Attachment1614a, Attachment1614h, Post}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import services.mocks.MockNonRepudiationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.accordion.Accordion
import uk.gov.hmrc.http.cache.client.CacheMap
import viewmodels.tasklist.{AttachmentsTaskList, TaskListSections}
import views.html.Summary

import scala.concurrent.Future

class SummaryControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with MockNonRepudiationService with FeatureToggleSupport {

  override protected def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  trait Setup {
    val testSummaryController = new SummaryController(
      mockSessionService,
      mockAuthClientConnector,
      mockVatRegistrationService,
      mockSummaryService,
      mockNonRepuidiationService,
      app.injector.instanceOf[Summary],
      mockBusinessService,
      mockAttachmentsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
    val mockAttachmentTaskList: AttachmentsTaskList.type = AttachmentsTaskList
    val mockTaskListSections: TaskListSections.type = TaskListSections
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.SummaryController.show)

  "Calling summary to show the summary page" when {
    "the StoreAnswersForNrs feature switch is enabled" should {
      "return OK with a valid summary view" in new Setup {
        when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

        when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

        when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

        when(mockSummaryService.getSummaryData(any(), any(), any(), any(), any())).thenReturn(Future.successful(Accordion()))

        mockStoreEncodedUserAnswers(regId)(Future.successful(""))

        callAuthorised(testSummaryController.show)(status(_) mustBe OK)
      }

      "return SEE_OTHER" in new Setup{
        when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(emptyVatScheme))

        when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

        when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

        when(mockSummaryService.getSummaryData(any(), any(), any(), any(), any())).thenReturn(Future.successful(Accordion()))

        mockStoreEncodedUserAnswers(regId)(Future.successful(""))

        callAuthorised(testSummaryController.show)(status(_) mustBe SEE_OTHER)
      }
    }
  }

  "Calling submitRegistration" should {

    "redirect to the application progress page when the status of the application in not complete" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(emptyVatScheme))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the confirmation page if the status of the document is in draft" in new Setup {

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "redirect to the cover letter preview page when the attachment method is Post and VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(
        fullVatSchemeAttachment.copy(attachments = fullVatSchemeAttachment.attachments.map(_.copy(method = Some(Post))))
      ))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.attachments.routes.CoverLetterPreviewController.show.url)
      }
    }

    "redirect to the confirmation page when the attachment method is Post and VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(
        fullVatSchemeAttachment.copy(attachments = fullVatSchemeAttachment.attachments.map(_.copy(method = Some(Post))))
      ))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Success))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "redirect to the submission in progress page if the status of the document is in progress" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(SubmissionInProgress))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.routes.SubmissionInProgressController.show.url)
      }
    }

    "redirect to the Submission Failed page if a submission has already been made" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(AlreadySubmitted))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result.redirectsTo(controllers.errors.routes.ErrorController.alreadySubmitted.url)
      }
    }

    "redirect to the Submission Failed Retryable page when Submission Fails but is Retryable" in new Setup {

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(SubmissionFailedRetryable))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.errors.routes.ErrorController.submissionRetryable.url

      }
    }

    "redirect to the Submission Failed page when Submission Fails" in new Setup {

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(SubmissionFailed))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.errors.routes.ErrorController.submissionFailed.url
      }
  }

    "redirect to the contact page" in new Setup {

      when(mockVatRegistrationService.getVatScheme(any(), any(), any())).thenReturn(Future.successful(fullVatSchemeAttachment))

      when(mockAttachmentsService.getAttachmentList(any())(any(), any())).thenReturn(Future.successful(List(Attachment1614a, Attachment1614h)))

      when(mockAttachmentsService.getIncompleteAttachments(any())(any(), any())).thenReturn(Future.successful(List()))

      when(mockVatRegistrationService.submitRegistration()(any(), any(), any(), any()))
        .thenReturn(Future.successful(Contact))

      when(mockSessionService.cache[CurrentProfile](any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map())))

      submitAuthorised(testSummaryController.submitRegistration, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
        (result: Future[Result]) =>
          await(result).header.status mustBe Status.SEE_OTHER
          result redirectsTo controllers.errors.routes.ErrorController.contact.url
      }
    }
  }
}