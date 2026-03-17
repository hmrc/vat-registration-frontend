/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.errors

import fixtures.VatRegistrationFixture
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.errors.BankDetailsLockoutPage

class BankDetailsLockoutControllerISpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  val view: BankDetailsLockoutPage = app.injector.instanceOf[BankDetailsLockoutPage]

  trait Setup {
    val testController = new BankDetailsLockoutController(
      view,
      mockSessionService,
      mockAuthClientConnector
    )
  }

  "show" should {
    "return 200" in new Setup {
      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }
  }
}