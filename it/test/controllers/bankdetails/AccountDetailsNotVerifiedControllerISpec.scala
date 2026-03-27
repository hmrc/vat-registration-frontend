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
import repositories.BarsLockRepository
import services.LockService
import testHelpers.{ControllerSpec, FutureAssertions}
import views.html.bankdetails.AccountDetailsNotVerifiedView

import scala.concurrent.Future

class AccountDetailsNotVerifiedControllerISpec extends ControllerSpec with VatRegistrationFixture with FutureAssertions {

  val mockBarsLockRepository: BarsLockRepository = mock[BarsLockRepository]
  val lockService: LockService                   = new LockService(mockBarsLockRepository)
  val view: AccountDetailsNotVerifiedView        = app.injector.instanceOf[AccountDetailsNotVerifiedView]

  trait Setup {
    val testController = new AccountDetailsNotVerifiedController(
      mockAuthClientConnector,
      mockSessionService,
      lockService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" should {
    "redirect to BankDetailsLockoutController when bars is locked" in new Setup {
      when(mockBarsLockRepository.isLocked(any()))
        .thenReturn(Future.successful(true))

      callAuthorised(testController.show) { result =>
        status(result)           mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.errors.routes.BankDetailsLockoutController.show.url)
      }
    }

    "return OK when bars is not locked" in new Setup {
      when(mockBarsLockRepository.isLocked(any()))
        .thenReturn(Future.successful(false))
      when(mockBarsLockRepository.getAttemptsUsed(any()))
        .thenReturn(Future.successful(2))

      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
      }
    }
  }
}
