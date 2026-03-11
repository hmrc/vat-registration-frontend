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

package controllers.bankdetails

import fixtures.VatRegistrationFixture
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import services.LockService
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.bankdetails.AccountDetailsNotVerifiedView

import scala.concurrent.Future

class AccountDetailsNotVerifiedSpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  val mockLockService: LockService = mock[LockService]
  val view: AccountDetailsNotVerifiedView = app.injector.instanceOf[AccountDetailsNotVerifiedView]

  trait Setup {
    val testController = new AccountDetailsNotVerified(
      mockAuthClientConnector,
      mockSessionService,
      mockLockService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" should {
    "return 200 when attempts used is below the lockout limit" in new Setup {
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(1))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "return 200 when attempts used is one below the lockout limit" in new Setup {
      // knownFactsLockAttemptLimit is 3 in appConfig, so 2 attempts should still show the page
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(2))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }

    "redirect to ThirdAttemptLockout when attempts used is at or above the lockout limit" in new Setup {
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(3)) // >= appConfig.knownFactsLockAttemptLimit (3)

      callAuthorised(testController.show) { result =>
        status(result)           mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.errors.routes.ThirdAttemptLockoutController.show.url)
      }
    }

    "redirect to ThirdAttemptLockout when attempts used exceeds the lockout limit" in new Setup {
      when(mockLockService.getBarsAttemptsUsed(any()))
        .thenReturn(Future.successful(5))

      callAuthorised(testController.show) { result =>
        status(result)           mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.errors.routes.ThirdAttemptLockoutController.show.url)
      }
    }
  }
}

