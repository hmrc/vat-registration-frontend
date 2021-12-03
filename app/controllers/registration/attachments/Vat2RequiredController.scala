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

package controllers.registration.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import featureswitch.core.config.EmailAttachments
import models.api.Post
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile}
import views.html.Vat2Required

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Vat2RequiredController @Inject()(view: Vat2Required,
                                       attachmentsService: AttachmentsService,
                                       val authConnector: AuthClientConnector,
                                       val keystoreConnector: KeystoreConnector)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfileNoStatusCheck {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(view()))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        if (isEnabled(EmailAttachments)) {
          Future.successful(Redirect(routes.AttachmentMethodController.show))
        } else {
          attachmentsService.storeAttachmentDetails(profile.registrationId, Post).map { _ =>
            Redirect(routes.DocumentsPostController.show)
          }
        }
  }

}
