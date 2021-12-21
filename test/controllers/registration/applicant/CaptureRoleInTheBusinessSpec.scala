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

import akka.actor.TypedActor.dispatcher
import fixtures.ApplicantDetailsFixtures
import models.Director
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.mocks.MockApplicantDetailsService
import testHelpers.ControllerSpec
import views.html.role_in_the_business

class CaptureRoleInTheBusinessSpec extends ControllerSpec
  with FutureAwaits
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures {

  trait Setup {
    val view: role_in_the_business = app.injector.instanceOf[role_in_the_business]
    val controller: CaptureRoleInTheBusinessController = new CaptureRoleInTheBusinessController(
      view,
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))


    val fakeRequest = FakeRequest(routes.CaptureRoleInTheBusinessController.show)

    val incompleteApplicantDetails = emptyApplicantDetails.copy(roleInTheBusiness = Some(Director))

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
        submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody()) {
          result => status(result) mustBe BAD_REQUEST
        }
      }

      "Redirect to FormerName with complete data" in new Setup {
        val role = "Director"

        mockSaveApplicantDetails(Director)(emptyApplicantDetails)

        submitAuthorised(controller.submit, fakeRequest.withFormUrlEncodedBody("roleInTheBusiness" -> role)) {
          result =>
            redirectLocation(result) mustBe Some(routes.FormerNameController.show)
        }
      }
    }
  }

}