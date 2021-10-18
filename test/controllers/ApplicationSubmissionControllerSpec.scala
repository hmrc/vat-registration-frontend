/*
 * Copyright 2021 HM Revenue & Customs
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

import fixtures.VatRegistrationFixture
import models.api.{Attached, Attachments, EmailMethod, IdentityEvidence, Other, Post}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Session
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.pages.application_submission_confirmation

import scala.concurrent.Future

class ApplicationSubmissionControllerSpec extends ControllerSpec with FutureAssertions with MockVatRegistrationService with VatRegistrationFixture {

  val applicationSubmissionConfirmationView: application_submission_confirmation =
    fakeApplication.injector.instanceOf[application_submission_confirmation]

  val testController = new ApplicationSubmissionController(
    vatRegistrationServiceMock,
    mockAttachmentsService,
    mockAuthClientConnector,
    mockKeystoreConnector,
    applicationSubmissionConfirmationView
  )

  s"GET ${routes.ApplicationSubmissionController.show()}" should {
    "display the submission confirmation page to the user" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(None, List())))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }
    "display the submission confirmation page to the user when IdentityEvidence is available" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(None, List(IdentityEvidence))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Other" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(Some(Other), List(IdentityEvidence))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Attached" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(Some(Attached), List(IdentityEvidence))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Post" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(Some(Post), List(IdentityEvidence))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is EmailMethod" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(Attachments(Some(EmailMethod), List(IdentityEvidence))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }
  }

  s"POST ${routes.ApplicationSubmissionController.submit()}" should {
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
