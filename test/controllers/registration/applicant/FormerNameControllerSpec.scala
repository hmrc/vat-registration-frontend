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

package controllers.registration.applicant

import controllers.registration.applicant.{routes => applicantRoutes}
import fixtures.{ApplicantDetailsFixtures, VatRegistrationFixture}
import models.api.{NETP, UkCompany}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.ApplicantDetailsService.HasFormerName
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.FormerName

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormerNameControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with VatRegistrationFixture
  with MockVatRegistrationService {

  trait Setup {
    val controller: FormerNameController = new FormerNameController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      vatRegistrationServiceMock,
      app.injector.instanceOf[FormerName]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.FormerNameController.show)

  val testApplicantDetails = emptyApplicantDetails.copy(hasFormerName = Some(true))

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(testApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(testApplicantDetails)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
  }

  "submit" should {
    "return BAD_REQUEST with Empty data" in new Setup {
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("formerNameRadio" -> "")){ result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "Redirect to ALF if no former name" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockSaveApplicantDetails(HasFormerName(false))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("value" -> "false")) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf.url)
      }
    }

    "Redirect to International Address capture if no former name and NETP" in new Setup {
      mockPartyType(Future.successful(NETP))
      mockSaveApplicantDetails(HasFormerName(false))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("value" -> "false")) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.InternationalHomeAddressController.show.url)
      }
    }

    "Redirect to FormerNameCapture with valid data with has former name" in new Setup {
      mockSaveApplicantDetails(HasFormerName(true))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "true"
      )) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.FormerNameCaptureController.show.url)
      }
    }
  }

}
