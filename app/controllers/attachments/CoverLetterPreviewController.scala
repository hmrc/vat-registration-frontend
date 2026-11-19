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

package controllers.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService}
import views.html.attachments.CoverLetterPreview

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CoverLetterPreviewController @Inject()(val authConnector: AuthClientConnector,
                                             val sessionService: SessionService,
                                             view: CoverLetterPreview)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  val show: Action[AnyContent] = isAuthenticatedAndSubmitted {
    implicit request => _ =>
      if (isEnabled(VrsNewAttachmentJourney)) {
        Future.successful(Ok(view()))
      } else {
        infoLog(s"[CoverLetterPreviewController][show] - VrsNewAttachmentJourney is disabled, redirecting to the application submitted page")
        Future.successful(Redirect(controllers.routes.ApplicationSubmissionController.show))
      }
  }
}
