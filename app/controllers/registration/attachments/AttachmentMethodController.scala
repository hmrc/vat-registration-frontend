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
import forms.AttachmentMethodForm
import models.api.{Attachments, EmailMethod, Post}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile}
import views.html.ChooseAttachmentMethod

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentMethodController @Inject()(val authConnector: AuthClientConnector,
                                           val keystoreConnector: KeystoreConnector,
                                           attachmentsService: AttachmentsService,
                                           form: AttachmentMethodForm,
                                           view: ChooseAttachmentMethod)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    attachmentsService.getAttachmentList(profile.registrationId).map {
      case Attachments(Some(method), _) =>
        Ok(view(form().fill(method)))
      case _ =>
        Ok(view(form()))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      attachmentMethod =>
        attachmentsService
          .storeAttachmentDetails(profile.registrationId, attachmentMethod)
          .map { _ =>
            attachmentMethod match {
              case Post =>
                Redirect(routes.DocumentsPostController.show())
              case EmailMethod =>
                Redirect(routes.EmailDocumentsController.show())
              case _ =>
                BadRequest(view(form().fill(attachmentMethod)))
            }
          }
    )
  }

}