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

package controllers

import fixtures.VatRegistrationFixture
import models.ContactPreference
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.contact_preference

import scala.concurrent.Future

class ContactPreferenceControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  val view = app.injector.instanceOf[contact_preference]

  class Setup {
    val controller: ContactPreferenceController = new ContactPreferenceController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockBusinessContactService,
      view
    )
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  class SubmissionSetup extends Setup {
    when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(validBusinessContactDetails))

    when(mockBusinessContactService.updateBusinessContact[ContactPreference](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(validBusinessContactDetails.contactPreference.get))

  }

  "showing the contact preference page" should {
    "return a 200" when {
      "everything is okay" in new SubmissionSetup {
        callAuthorised(controller.showContactPreference) {
          _ isA 200
        }
      }
    }
    "throw an exception" when {
      "getBusinessContact Fails" in new Setup {
        when(mockBusinessContactService.getBusinessContact(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showContactPreference) {
          _ failedWith exception
        }
      }
    }
  }

  "submitting the contact preference page" should {
    val fakeRequest = FakeRequest(routes.ContactPreferenceController.showContactPreference())

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withFormUrlEncodedBody()) {
          _ isA 400
        }
      }
    }

    "return a 400" when {
      "user provides invalid data" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withFormUrlEncodedBody("value" -> "BadStuff")) {
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects email and redirect to the business activity description" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withFormUrlEncodedBody("value" -> "email")) {
          _ redirectsTo controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show().url
        }
      }
    }

    "return a 303" when {
      "user selects letter and redirect to the business activity description" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withFormUrlEncodedBody("value" -> "letter")) {
          _ redirectsTo controllers.registration.sicandcompliance.routes.BusinessActivityDescriptionController.show().url
        }
      }
    }

    "return an exception" when {
      "updateBusinessContact fails" in new SubmissionSetup {
        when(mockBusinessContactService.updateBusinessContact[ContactPreference](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(throw exception))

        submitAuthorised(controller.submitContactPreference, fakeRequest.withFormUrlEncodedBody("value" -> "email")) {
          _ failedWith exception
        }
      }
    }
  }
}