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

import fixtures.ApplicantDetailsFixtures
import models.TelephoneNumber
import models.api.{NETP, UkCompany}
import models.external.{EmailAddress, EmailVerified, Name}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.applicant.capture_telephone_number

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CaptureTelephoneNumberControllerSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with MockVatRegistrationService {

  trait Setup {
    val view: capture_telephone_number = app.injector.instanceOf[capture_telephone_number]
    val controller: CaptureTelephoneNumberController = new CaptureTelephoneNumberController(
      view,
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      vatRegistrationServiceMock
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(routes.CaptureTelephoneNumberController.show)
  val incompleteApplicantDetails = emptyApplicantDetails.copy(
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    hasFormerName = Some(true),
    formerName = Some(Name(Some("Old"), last = "Name"))
  )

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
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
      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody()){
        result => status(result) mustBe BAD_REQUEST
      }
    }
    "return SEE_OTHER with valid Contact Details entered" in new Setup {
      val phone = "01234567891"

      mockSaveApplicantDetails(TelephoneNumber(phone))(emptyApplicantDetails)
      mockPartyType(Future.successful(UkCompany))

      submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("telephone-number" -> phone)) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(controllers.business.routes.PpobAddressController.startJourney.url)
      }
    }
   "return SEE_OTHER with valid Contact Details entered for a NETP" in new Setup {
     val phone = "01234567891"

     mockSaveApplicantDetails(TelephoneNumber(phone))(emptyApplicantDetails)
     mockPartyType(Future.successful(NETP))

     submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("telephone-number" -> phone)) { res =>
         status(res) mustBe SEE_OTHER
         redirectLocation(res) mustBe Some(controllers.business.routes.InternationalPpobAddressController.show.url)
     }
   }
  }

}
