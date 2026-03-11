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


import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import config.FrontendAppConfig
import controllers.errors
import repositories.UserLockRepository
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockService @Inject()(userLockRepository: UserLockRepository,
                            config: FrontendAppConfig)(implicit ec: ExecutionContext) extends LoggingUtil{



  def updateAttempts(userId: String): Future[Map[String, Int]] = {
    if (config.isKnownFactsCheckEnabled) {
      userLockRepository.updateAttempts(userId)
    } else {
      Future.successful(Map.empty)
    }
  }

  def isJourneyLocked(userId: String): Future[Boolean] = {
    if (config.isKnownFactsCheckEnabled) {
      userLockRepository.isUserLocked(userId)
    } else {
      Future.successful(false)
    }
  }

  // ---- BARs bank account lock methods ----

  def getBarsAttemptsUsed(registrationId: String): Future[Int] =
    userLockRepository.getFailedAttempts(registrationId)

  def incrementBarsAttempts(registrationId: String): Future[Int] =
    userLockRepository.updateAttempts(registrationId).map(_.getOrElse("user", 0))

  def isBarsLocked(registrationId: String): Future[Boolean] =
    userLockRepository.isUserLocked(registrationId)
}
