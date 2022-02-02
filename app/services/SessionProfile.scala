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

package services

import common.enums.VatRegStatus
import controllers.routes
import models.CurrentProfile
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait SessionProfile {

  implicit val executionContext: ExecutionContext
  val sessionService: SessionService
  private val CURRENT_PROFILE_KEY = "CurrentProfile"

  def withCurrentProfile(checkStatus: Boolean = true)(f: CurrentProfile => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    sessionService.fetchAndGet[CurrentProfile](CURRENT_PROFILE_KEY) flatMap { currentProfile =>
      currentProfile.fold(
        Future.successful(Redirect(routes.JourneyController.show))
      ) {
        profile =>
          profile.vatRegistrationStatus match {
            case VatRegStatus.draft => f(profile)
            case VatRegStatus.submitted if checkStatus => Future.successful(Redirect(routes.ApplicationSubmissionController.show))
            case VatRegStatus.failedRetryable if checkStatus => Future.successful(Redirect(routes.ErrorController.submissionRetryable))
            case VatRegStatus.failed if checkStatus => Future.successful(Redirect(routes.ErrorController.submissionFailed))
            case VatRegStatus.duplicateSubmission if checkStatus => Future.successful(Redirect(routes.ErrorController.alreadySubmitted))
            case VatRegStatus.locked if checkStatus => Future.successful(Redirect(routes.SubmissionInProgressController.show))
            case _ if !checkStatus => f(profile)
          }
      }
    }
  }

  def getProfile(implicit hc: HeaderCarrier): Future[Option[CurrentProfile]] = {
    sessionService.fetchAndGet[CurrentProfile](CURRENT_PROFILE_KEY)
  }
}
