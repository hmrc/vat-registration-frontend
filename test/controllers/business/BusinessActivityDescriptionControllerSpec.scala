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

package controllers.business

import fixtures.VatRegistrationFixture
import play.api.test.FakeRequest
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.sicandcompliance.business_activity_description

import scala.concurrent.Future

class BusinessActivityDescriptionControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  class Setup {
    val view = app.injector.instanceOf[business_activity_description]
    val controller = new BusinessActivityDescriptionController(
      mockAuthClientConnector,
      mockSessionService,
      mockBusinessService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" should {
    "return OK" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
    "return OK where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetBusiness(Future.successful(validBusiness))

      callAuthorised(controller.show) { result =>
        status(result) mustBe OK
      }
    }
  }

  "submit" should {
    "return BAD_REQUEST when an invalid description is posted" in new Setup {
      submitAuthorised(controller.submit, FakeRequest().withFormUrlEncodedBody(
      ))(result => result isA BAD_REQUEST)
    }
    "Redirect to ICL when a valid description is posted" in new Setup {
      mockUpdateBusiness(Future.successful(validBusinessWithNoDescription))

      submitAuthorised(controller.submit, FakeRequest().withFormUrlEncodedBody("description" -> "Testing")) {
        _ redirectsTo s"$contextRoot/choose-standard-industry-classification-codes"
      }
    }
  }

}
