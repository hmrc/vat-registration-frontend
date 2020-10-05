/*
 * Copyright 2020 HM Revenue & Customs
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
import models.view.{ApplicantDetails, ContactDetailsView, FormerNameView}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import testHelpers.ControllerSpec

import scala.concurrent.ExecutionContext.Implicits.global

class ContactDetailsControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  trait Setup {
    val controller: ContactDetailsController = new ContactDetailsController(
      messagesControllerComponents,
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockApplicantDetailsService,
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.ContactDetailsController.show())



  val incompleteApplicantDetails = emptyApplicantDetails.copy(
    contactDetails = Some(ContactDetailsView(Some("t@t.tt.co"))),
    formerName = Some(FormerNameView(true, Some("Old Name")))
  )

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)

      callAuthorised(controller.show()) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)

      callAuthorised(controller.show()) {
        status(_) mustBe OK
      }
    }
  }

 "submit" should {
    "return BAD_REQUEST with Empty data" in new Setup {
      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody()){
        result => status(result) mustBe BAD_REQUEST
      }
    }

    "return SEE_OTHER with valid Contact Details entered" in new Setup {
      val email = "some@email.com"
      val daytimePhone = "01234567891"
      val mobile = "07234567891"

      mockSaveApplicantDetails(ContactDetailsView(Some(daytimePhone), Some(email), Some(mobile)))(emptyApplicantDetails)

      submitAuthorised(controller.submit(), fakeRequest.withFormUrlEncodedBody(
        "email" -> email,
        "daytimePhone" -> daytimePhone,
        "mobile" -> mobile
      )) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf().url)
      }
    }
  }

}
