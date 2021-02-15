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

package controllers.registration.applicant

import controllers.registration.applicant.{routes => applicantRoutes}
import fixtures.ApplicantDetailsFixtures
import mocks.mockservices.MockApplicantDetailsService
import models.view.{FormerNameDateView, FormerNameView}
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import testHelpers.ControllerSpec

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class FormerNameDateControllerSpec extends ControllerSpec
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  trait Setup {
    val controller: FormerNameDateController = new FormerNameDateController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockApplicantDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.FormerNameDateController.show())

  val incompleteApplicantDetails = emptyApplicantDetails
    .copy(formerName = Some(FormerNameView(true, Some("Old Name"))),transactorDetails = Some(testTransactorDetails))

  val incompleteApplicantDetailsDate = incompleteApplicantDetails
    .copy(formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 6, 23))),
      transactorDetails = Some(testTransactorDetails))

  val onlyTranscatorDetails = emptyApplicantDetails.copy(transactorDetails = Some(testTransactorDetails))

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetailsDate)

      callAuthorised(controller.show()) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)

      val res = controller.show()(fakeRequest)

      status(res) mustBe OK
    }

    "throw an IllegalStateException when the former name is missing" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)

      intercept[IllegalStateException] {
        await(controller.show()(fakeRequest))
      }
    }
  }

  "submit" should {
    "return BAD_REQUEST with Empty data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)

      val res = controller.submit()(FakeRequest().withFormUrlEncodedBody("" -> ""))

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) mustBe BAD_REQUEST
      }
    }

    "Redirect to ContactDetails when Former name Date selected" in new Setup {
      mockGetApplicantDetails(currentProfile)(onlyTranscatorDetails)
      mockSaveApplicantDetails(FormerNameDateView(LocalDate.parse("2020-02-01")))(onlyTranscatorDetails)

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "formerNameDate.day" -> "1",
        "formerNameDate.month" -> "2",
        "formerNameDate.year" -> "2020"
      )) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf().url)
      }
    }
  }
}
