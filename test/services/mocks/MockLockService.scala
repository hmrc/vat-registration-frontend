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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.LockService

import scala.concurrent.Future

trait MockLockService {
  this: MockitoSugar  =>

  val mockLockService: LockService = mock[LockService]

  def mockGetBarsAttemptsUsed(registrationId: String)(response: Future[Int]): Unit =
    when(mockLockService.getBarsAttemptsUsed(ArgumentMatchers.eq(registrationId)))
      .thenReturn(response)

  def mockIncrementBarsAttempts(registrationId: String)(response: Future[Int]): Unit =
    when(mockLockService.incrementBarsAttempts(ArgumentMatchers.eq(registrationId)))
      .thenReturn(response)

  def mockIsBarsLocked(registrationId: String)(response: Future[Boolean]): Unit =
    when(mockLockService.isBarsLocked(ArgumentMatchers.eq(registrationId)))
      .thenReturn(response)
}
