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

package controllers.errors

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import play.api.mvc._
import services.{SessionProfile, SessionService}
import views.html.errors.{AlreadySubmittedKickout, ContactView, SubmissionFailed, SubmissionRetryableView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorController @Inject()(val authConnector: AuthClientConnector,
                                val sessionService: SessionService,
                                submissionFailedView: SubmissionFailed,
                                submissionRetryableView: SubmissionRetryableView,
                                alreadySubmittedView: AlreadySubmittedKickout,
                                contactView: ContactView)
                               (implicit appConfig: FrontendAppConfig,
                                val executionContext: ExecutionContext,
                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ => Future.successful(Ok(submissionRetryableView()))
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ => Future.successful(Ok(submissionFailedView()))
  }

  def alreadySubmitted: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ => Future.successful(Ok(alreadySubmittedView()))
  }

  def alreadySubmittedSignOut: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    _ => _ => Future.successful(Redirect(controllers.callbacks.routes.SignInOutController.signOut))
  }

  def contact: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ => Future.successful(Ok(contactView()))
  }
}
