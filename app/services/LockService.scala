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

import models.CurrentProfile
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.BarsLockRepository
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockService @Inject() (barsLockRepository: BarsLockRepository) extends LoggingUtil {

  def getBarsAttemptsUsed(registrationId: String): Future[Int] =
    barsLockRepository.getAttemptsUsed(registrationId)

  def incrementBarsAttempts(registrationId: String): Future[Int] =
    barsLockRepository.recordFailedAttempt(registrationId)

  def isBarsLocked(registrationId: String): Future[Boolean] =
    barsLockRepository.isLocked(registrationId)

  def redirectIfBarsIsLocked(notLockedScenario: => Future[Result])(implicit profile: CurrentProfile, ec: ExecutionContext): Future[Result] =
    isBarsLocked(profile.registrationId).flatMap { isLocked =>
      if (isLocked) Future.successful(Redirect(controllers.errors.routes.BankDetailsLockoutController.show)) else notLockedScenario
    }

}
