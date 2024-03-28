/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.business

import featuretoggle.FeatureToggleSupport
import fixtures.VatRegistrationFixture
import models.ContactPreference
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.business.ContactPreferenceView

import scala.concurrent.Future

class ContactPreferenceControllerSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions with FeatureToggleSupport {

  val view: ContactPreferenceView = app.injector.instanceOf[ContactPreferenceView]

  class Setup {
    val controller: ContactPreferenceController = new ContactPreferenceController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      view
    )
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  class SubmissionSetup extends Setup {
    when(mockBusinessService.getBusiness(ArgumentMatchers.any(), ArgumentMatchers.any(), any()))
      .thenReturn(Future(validBusiness))

    when(mockBusinessService.updateBusiness[ContactPreference](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), any()))
      .thenReturn(Future(validBusiness))

  }

  "showing the contact preference page" should {
    "return a 200" when {
      "contract preference is available" in new SubmissionSetup {
        callAuthorised(controller.showContactPreference) {
          _ isA 200
        }
      }

      "contract preference is unavailable" in new Setup {
        when(mockBusinessService.getBusiness(ArgumentMatchers.any(), ArgumentMatchers.any(), any()))
          .thenReturn(Future(validBusiness.copy(contactPreference = None)))

        callAuthorised(controller.showContactPreference) {
          _ isA 200
        }
      }
    }

    "throw an exception" when {
      "getBusinessContact Fails" in new Setup {
        when(mockBusinessService.getBusiness(ArgumentMatchers.any(), ArgumentMatchers.any(), any()))
          .thenReturn(Future(throw exception))

        callAuthorised(controller.showContactPreference) {
          _ failedWith exception
        }
      }
    }
  }

  "submitting the contact preference page" should {
    val fakeRequest = FakeRequest(routes.ContactPreferenceController.showContactPreference)

    "return a 400" when {
      "form is empty" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withMethod("POST").withFormUrlEncodedBody()) {
          _ isA 400
        }
      }
    }

    "return a 400" when {
      "user provides invalid data" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "BadStuff")) {
          _ isA 400
        }
      }
    }

    "return a 303" when {
      "user selects email and redirect to the Task List" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "email")) {
          _ redirectsTo controllers.routes.TaskListController.show.url
        }
      }

      "user selects letter and redirect to the Task List" in new SubmissionSetup {
        submitAuthorised(controller.submitContactPreference, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "letter")) {
          _ redirectsTo controllers.routes.TaskListController.show.url
        }
      }
    }

    "return an exception" when {
      "updateBusinessContact fails" in new SubmissionSetup {
        when(mockBusinessService.updateBusiness[ContactPreference](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), any()))
          .thenReturn(Future(throw exception))

        submitAuthorised(controller.submitContactPreference, fakeRequest.withMethod("POST").withFormUrlEncodedBody("value" -> "email")) {
          _ failedWith exception
        }
      }
    }
  }
}