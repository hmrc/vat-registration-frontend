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

package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import featureswitch.core.config.FeatureSwitching
import fixtures.{ApplicantDetailsFixtures, VatRegistrationFixture}
import models.api.UkCompany
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.ApplicantDetailsService.HasFormerName
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.applicant.FormerName

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormerNameControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with VatRegistrationFixture
  with MockVatRegistrationService
  with FeatureSwitching {

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

  "submit" when {
    "redirect to the Task List" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockSaveApplicantDetails(HasFormerName(false))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "false")) { result =>
        redirectLocation(result) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
    "return BAD_REQUEST if the posted data is invalid" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "")) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }

}
