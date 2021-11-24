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

package controllers

import fixtures.VatRegistrationFixture
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.pages.application_progress_saved

class ApplicationProgressSavedControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  val applicationProgressSavedView: application_progress_saved =
    fakeApplication.injector.instanceOf[application_progress_saved]

  val testController = new ApplicationProgressSavedController(
    mockReturnsService,
    mockAuthClientConnector,
    mockKeystoreConnector,
    applicationProgressSavedView
  )

  s"GET ${routes.ApplicationProgressSavedController.show}" should {
    "display the submission progress saved page to the user" in {
      mockAuthenticated()
      mockWithCurrentProfile(Some(currentProfile))

      callAuthorised(testController.show) { res =>
        status(res) mustBe OK
      }
    }
  }
}
