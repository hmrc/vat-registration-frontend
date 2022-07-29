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

import fixtures.VatRegistrationFixture
import models.api._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.mvc.Session
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.ApplicationSubmissionConfirmation

import scala.concurrent.Future

class ApplicationSubmissionControllerSpec extends ControllerSpec with FutureAssertions with MockVatRegistrationService with VatRegistrationFixture {

  val applicationSubmissionConfirmationView: ApplicationSubmissionConfirmation =
    fakeApplication.injector.instanceOf[ApplicationSubmissionConfirmation]

  val testController = new ApplicationSubmissionController(
    vatRegistrationServiceMock,
    mockAttachmentsService,
    mockAuthClientConnector,
    mockSessionService,
    applicationSubmissionConfirmationView
  )

  s"GET ${routes.ApplicationSubmissionController.show}" should {
    "display the submission confirmation page to the user" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List()))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = None))))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(IdentityEvidence)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = None))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Other" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(IdentityEvidence)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = Some(Other)))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Attached" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(IdentityEvidence)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = Some(Attached)))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is Post" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(IdentityEvidence)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = Some(Post)))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when IdentityEvidence is available and Method is EmailMethod" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(IdentityEvidence)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = Some(EmailMethod)))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }

    "display the submission confirmation page to the user when VAT51 is available" in {
      mockAuthenticatedBasic
      mockWithCurrentProfile(Some(currentProfile))

      when(mockAttachmentsService.getAttachmentList(any())(any()))
        .thenReturn(Future.successful(List(VAT51)))

      when(mockAttachmentsService.getAttachmentDetails(any())(any()))
        .thenReturn(Future.successful(Some(Attachments(method = Some(Post)))))

      when(vatRegistrationServiceMock.getAckRef(ArgumentMatchers.eq(validVatScheme.id))(any()))
        .thenReturn(Future.successful("123412341234"))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
        contentAsString(res) must include("You can print the cover letter here (opens in new tab)")
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
