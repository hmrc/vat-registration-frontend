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

package controllers

import common.enums.VatRegStatus
import fixtures.VatRegistrationFixture
import models.api._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Session
import services.mocks.{MockApplicantDetailsService, MockTransactorDetailsService, MockVatRegistrationService}
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.ApplicationSubmissionConfirmation

import scala.concurrent.Future

class ApplicationSubmissionControllerSpec extends ControllerSpec with FutureAssertions with MockVatRegistrationService with VatRegistrationFixture with MockTransactorDetailsService with MockApplicantDetailsService {

  val applicationSubmissionConfirmationView: ApplicationSubmissionConfirmation =
    fakeApplication.injector.instanceOf[ApplicationSubmissionConfirmation]

  val testController = new ApplicationSubmissionController(
    vatRegistrationServiceMock,
    mockApplicantDetailsService,
    mockTransactorDetailsService,
    mockAttachmentsService,
    mockAuthClientConnector,
    mockSessionService,
    applicationSubmissionConfirmationView
  )

  s"GET ${routes.ApplicationSubmissionController.show}" when {
    "the registration status is submitted" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)
      "display the submission confirmation page to the user" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.registrationId))(any()))
          .thenReturn(Future.successful("123412341234"))

        when(mockAttachmentsService.getAttachmentList(any())(any()))
          .thenReturn(Future.successful(List()))

        when(mockAttachmentsService.getAttachmentDetails(any())(any()))
          .thenReturn(Future.successful(Some(Attachments(method = None))))

        when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        mockIsTransactor(Future.successful(true))
        mockGetTransactorDetails(profile)(validTransactorDetails)

        callAuthorised(testController.show) { res =>
          status(res) mustBe OK
        }
      }

      "display the submission confirmation page to the user when IdentityEvidence is available" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        when(mockAttachmentsService.getAttachmentList(any())(any()))
          .thenReturn(Future.successful(List(IdentityEvidence)))

        when(mockAttachmentsService.getAttachmentDetails(any())(any()))
          .thenReturn(Future.successful(Some(Attachments(method = None))))

        when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.registrationId))(any()))
          .thenReturn(Future.successful("123412341234"))

        when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        mockIsTransactor(Future.successful(false))
        mockGetApplicantDetails(profile)(completeApplicantDetails)

        callAuthorised(testController.show) { res =>
          status(res) mustBe OK
        }
      }

      "display the submission confirmation page to the user when IdentityEvidence is available and Method is Attached" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        when(mockAttachmentsService.getAttachmentList(any())(any()))
          .thenReturn(Future.successful(List(IdentityEvidence)))

        when(mockAttachmentsService.getAttachmentDetails(any())(any()))
          .thenReturn(Future.successful(Some(Attachments(method = Some(Attached)))))

        when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.registrationId))(any()))
          .thenReturn(Future.successful("123412341234"))

        when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        mockIsTransactor(Future.successful(false))
        mockGetApplicantDetails(profile)(completeApplicantDetails)

        callAuthorised(testController.show) { res =>
          status(res) mustBe OK
        }
      }

      "display the submission confirmation page to the user when IdentityEvidence is available and Method is Post" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        when(mockAttachmentsService.getAttachmentList(any())(any()))
          .thenReturn(Future.successful(List(IdentityEvidence)))

        when(mockAttachmentsService.getAttachmentDetails(any())(any()))
          .thenReturn(Future.successful(Some(Attachments(method = Some(Post)))))

        when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.registrationId))(any()))
          .thenReturn(Future.successful("123412341234"))

        when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        mockIsTransactor(Future.successful(false))
        mockGetApplicantDetails(profile)(completeApplicantDetails)

        callAuthorised(testController.show) { res =>
          status(res) mustBe OK
        }
      }

      "display the submission confirmation page to the user when VAT51 is available" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        when(mockAttachmentsService.getAttachmentList(any())(any()))
          .thenReturn(Future.successful(List(VAT51)))

        when(mockAttachmentsService.getAttachmentDetails(any())(any()))
          .thenReturn(Future.successful(Some(Attachments(method = Some(Post)))))

        when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.registrationId))(any()))
          .thenReturn(Future.successful("123412341234"))

        when(vatRegistrationServiceMock.getEligibilitySubmissionData(any(), any()))
          .thenReturn(Future.successful(validEligibilitySubmissionData))

        mockIsTransactor(Future.successful(false))
        mockGetApplicantDetails(profile)(completeApplicantDetails)

        callAuthorised(testController.show) { res =>
          status(res) mustBe OK
        }
      }
    }

    "the registration status is draft" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.draft)
      "redirect to TaskList" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(routes.TaskListController.show.url)
        }
      }
    }

    "the registration status is contact" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.contact)
      "redirect to TaskList" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(routes.TaskListController.show.url)
        }
      }
    }

    "the registration status is failedRetryable" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.failedRetryable)
      "redirect to Submission retryable page" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(controllers.errors.routes.ErrorController.submissionRetryable.url)
        }
      }
    }
    "the registration status is failed" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.failed)
      "redirect to Submission failed page" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(controllers.errors.routes.ErrorController.submissionFailed.url)
        }
      }
    }
    "the registration status is duplicateSubmission" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.duplicateSubmission)
      "redirect to Submission duplicate page" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(controllers.errors.routes.ErrorController.alreadySubmitted.url)
        }
      }
    }
    "the registration status is locked" must {
      val profile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.locked)
      "redirect to Submission in progress page" in {
        mockAuthenticatedBasic
        mockWithCurrentProfile(Some(profile))

        callAuthorised(testController.show) { res =>
          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(routes.SubmissionInProgressController.show.url)
        }
      }
    }
  }

  s"POST ${routes.ApplicationSubmissionController.submit}" should {
    "redirect to the feedback form page" in {
      mockAuthenticated()

      callAuthorisedOrg(testController.submit) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(appConfig.feedbackUrl)
        session(res) mustBe Session.emptyCookie
      }
    }
  }
}
