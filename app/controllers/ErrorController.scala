/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.SessionProfile
import views.html.pages.error.submissionFailed

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorController @Inject()(val authConnector: AuthClientConnector,
                                val keystoreConnector: KeystoreConnector,
                                submissionFailedView: submissionFailed)
                               (implicit appConfig: FrontendAppConfig,
                                val executionContext: ExecutionContext,
                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def submissionRetryable: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ =>
        Future.successful(Ok(views.html.pages.error.submissionTimeout()))
  }

  def submissionFailed: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request => _ =>
        Future.successful(Ok(submissionFailedView()))
  }
}
