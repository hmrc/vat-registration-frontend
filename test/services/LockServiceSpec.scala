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

package services

import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.UserLockRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LockServiceSpec extends PlaySpec with MockitoSugar with FutureAwaits with DefaultAwaitTimeout {

  val mockUserLockRepository: UserLockRepository = mock[UserLockRepository]
  val mockAppConfig: FrontendAppConfig             = mock[FrontendAppConfig]

  val registrationId = "reg-123"

  trait Setup {
    val service: LockService = new LockService(mockUserLockRepository, mockAppConfig)
  }

  // ---- getBarsAttemptsUsed ----

  "getBarsAttemptsUsed" should {
    "return the number of failed attempts from the repository" in new Setup {
      when(mockUserLockRepository.getFailedAttempts(eqTo(registrationId)))
        .thenReturn(Future.successful(2))

      await(service.getBarsAttemptsUsed(registrationId)) mustBe 2
    }

    "return 0 when there are no recorded attempts" in new Setup {
      when(mockUserLockRepository.getFailedAttempts(eqTo(registrationId)))
        .thenReturn(Future.successful(0))

      await(service.getBarsAttemptsUsed(registrationId)) mustBe 0
    }
  }

  // ---- incrementBarsAttempts ----

  "incrementBarsAttempts" should {
    "return the new total number of failed attempts after incrementing" in new Setup {
      when(mockUserLockRepository.updateAttempts(eqTo(registrationId)))
        .thenReturn(Future.successful(Map("user" -> 1)))

      await(service.incrementBarsAttempts(registrationId)) mustBe 1
    }

    "return 0 if the repository map does not contain the 'user' key" in new Setup {
      when(mockUserLockRepository.updateAttempts(eqTo(registrationId)))
        .thenReturn(Future.successful(Map.empty[String, Int]))

      await(service.incrementBarsAttempts(registrationId)) mustBe 0
    }

    "return 3 on the third failed attempt" in new Setup {
      when(mockUserLockRepository.updateAttempts(eqTo(registrationId)))
        .thenReturn(Future.successful(Map("user" -> 3)))

      await(service.incrementBarsAttempts(registrationId)) mustBe 3
    }
  }

  // ---- isBarsLocked ----

  "isBarsLocked" should {
    "return true when the user is locked in the repository" in new Setup {
      when(mockUserLockRepository.isUserLocked(eqTo(registrationId)))
        .thenReturn(Future.successful(true))

      await(service.isBarsLocked(registrationId)) mustBe true
    }

    "return false when the user is not locked in the repository" in new Setup {
      when(mockUserLockRepository.isUserLocked(eqTo(registrationId)))
        .thenReturn(Future.successful(false))

      await(service.isBarsLocked(registrationId)) mustBe false
    }
  }
}

