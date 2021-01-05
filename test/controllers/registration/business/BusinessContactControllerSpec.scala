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

package controllers.registration.business

import fixtures.VatRegistrationFixture
import models.CompanyContactDetails
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}

import scala.concurrent.Future

class BusinessContactControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val controller: BusinessContactDetailsController = new BusinessContactDetailsController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockBusinessContactService,
      mockPrePopulationService,
      mockAddressLookupService
    ) {
      override lazy val dropoutUrl: String = "test otrs URL"
    }

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  class SubmissionSetup extends Setup {
    when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(validBusinessContactDetails))
  }

  "showing the company contact details page" should {
    "return OK" when {
      "everything is okay" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails))

        callAuthorised(controller.show) {
          _ isA 200
        }
      }

      "everything is okay and no address exists in the business contact" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.copy(companyContactDetails = None)))

        callAuthorised(controller.show) {
          _ isA 200
        }
      }
    }
    "throw an exception" when {
      "getBussinesContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.show) {
          _ failedWith exception
        }
      }
    }
  }

  "submitting the company contact details page" should {
    val fakeRequest = FakeRequest(controllers.registration.business.routes.BusinessContactDetailsController.show())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody()) {
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects other and redirect to the contact preference page" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(validBusinessContactDetails.companyContactDetails.get))

        submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com", "mobile" -> "1224456378387")) {
          _ redirectsTo controllers.routes.ContactPreferenceController.showContactPreference().url
        }
      }
    }

    "return an exception" when {
      "updateBusinessContact fails" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[CompanyContactDetails](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("email" -> "test@email.com", "mobile" -> "1224456378387")) {
          _ failedWith exception
        }
      }
    }
  }

}
