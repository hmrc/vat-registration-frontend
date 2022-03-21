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
import models.external.Name
import org.jsoup.Jsoup
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.FormerNameCapture

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormerNameCaptureControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with VatRegistrationFixture
  with MockVatRegistrationService {

  trait Setup {
    val controller: FormerNameCaptureController = new FormerNameCaptureController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      app.injector.instanceOf[FormerNameCapture]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.FormerNameCaptureController.show)
  val testName = Name(first = Some(testFirstName), last = testLastName)
  val testApplicantDetails = emptyApplicantDetails.copy(hasFormerName = Some(true), formerName = Some(testName))

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(testApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK with an empty first name box if the firstName is missing" in new Setup {
      val oldName = Name(None, None, testLastName, None)

      mockGetApplicantDetails(currentProfile)(testApplicantDetails.copy(formerName = Some(oldName)))
      mockGetTransactorApplicantName(currentProfile)(None)

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
        val doc = Jsoup.parse(contentAsString(result))
        doc.getElementById("formerFirstName").`val`() mustBe ""
        doc.getElementById("formerLastName").`val`() mustBe testLastName
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

    "Redirect to FormerNameDate with valid data with former name" in new Setup {
      mockSaveApplicantDetails(Name(Some(testFirstName), last = testLastName))(emptyApplicantDetails)

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody(
        "formerFirstName" -> testFirstName,
        "formerLastName" -> testLastName
      )) { result =>
        redirectLocation(result) mustBe Some(applicantRoutes.FormerNameDateController.show.url)
      }
    }
  }

}
