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
import models.api.IdentityEvidence
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile}
import views.html.DocumentsRequired

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocumentsRequiredController @Inject()(val authConnector: AuthClientConnector,
                                            val keystoreConnector: KeystoreConnector,
                                            attachmentsService: AttachmentsService,
                                            documentsRequiredPage: DocumentsRequired)
                                           (implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val resolve: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        attachmentsService.getAttachmentList(profile.registrationId).map { attachmentsList =>
          if (attachmentsList.attachments.contains(IdentityEvidence)) {
            Redirect(routes.DocumentsRequiredController.show())
          } else {
            Redirect(controllers.routes.SummaryController.show())
          }
        }
  }

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => _ =>
      Future.successful(Ok(documentsRequiredPage()))
  }
}
