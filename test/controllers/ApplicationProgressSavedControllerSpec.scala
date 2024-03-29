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

package controllers

import fixtures.VatRegistrationFixture
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.ApplicationProgressSaved

class ApplicationProgressSavedControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture {

  val applicationProgressSavedView: ApplicationProgressSaved =
    fakeApplication.injector.instanceOf[ApplicationProgressSaved]

  val testController = new ApplicationProgressSavedController(
    movkVatApplicationService,
    mockAuthClientConnector,
    mockSessionService,
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
