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

package controllers

import common.enums.VatRegStatus
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import models.CurrentProfile
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatRegistrationService}
import views.html.pages.SubmissionInProgress

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionInProgressController @Inject()(view: SubmissionInProgress,
                                               val authConnector: AuthClientConnector,
                                               val sessionService: SessionService,
                                               vatRegistrationService: VatRegistrationService)
                                              (implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(view()))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        for {
          status <- vatRegistrationService.getStatus(profile.registrationId)
          _ <- sessionService.cache[CurrentProfile]("CurrentProfile", profile.copy(vatRegistrationStatus = status))
          redirect = status match {
            case VatRegStatus.submitted => Redirect(controllers.routes.ApplicationSubmissionController.show)
            case VatRegStatus.locked => Redirect(controllers.routes.SubmissionInProgressController.show)
            case VatRegStatus.duplicateSubmission => Redirect(controllers.routes.ErrorController.alreadySubmitted)
            case VatRegStatus.failed => Redirect(controllers.routes.ErrorController.submissionFailed)
            case VatRegStatus.failedRetryable => Redirect(controllers.routes.ErrorController.submissionRetryable)
          }
        } yield redirect

  }
}
