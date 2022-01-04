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
import models.view.FormerNameView
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.former_name

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
      app.injector.instanceOf[former_name]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.FormerNameController.show)

  val incompleteApplicantDetails = emptyApplicantDetails.copy(formerName = Some(FormerNameView(true, Some("Old Name"))))

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
  }

  "submit" should {
    "return BAD_REQUEST with Empty data" in new Setup {

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("formerNameRadio" -> "")){ result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "Redirect to ALF if no former name" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockSaveApplicantDetails(FormerNameView(false))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("value" -> "false")) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf.url)
      }
    }

    "Redirect to International Address capture if no former name and NETP" in new Setup {
      mockPartyType(Future.successful(NETP))
      mockSaveApplicantDetails(FormerNameView(false))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("value" -> "false")) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.InternationalHomeAddressController.show.url)
      }
    }

    "Redirect to FormerNameDate with valid data with former name" in new Setup {
      mockSaveApplicantDetails(FormerNameView(true, Some("some name")))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "value" -> "true",
        "formerName" -> "some name"
      )) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.FormerNameDateController.show.url)
      }
    }
  }

}
