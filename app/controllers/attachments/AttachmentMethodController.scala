/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.AttachmentMethodForm
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import views.html.attachments.ChooseAttachmentMethod

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentMethodController @Inject()(val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           attachmentsService: AttachmentsService,
                                           upscanService: UpscanService,
                                           form: AttachmentMethodForm,
                                           view: ChooseAttachmentMethod)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    attachmentsService.getAttachmentDetails(profile.registrationId).map {
      case Some(Attachments(Some(method), _, _, _, _)) =>
        Ok(view(form().fill(method)))
      case _ =>
        Ok(view(form()))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      attachmentMethod => {
        attachmentsService
          .storeAttachmentDetails(profile.registrationId, attachmentMethod)
          .flatMap { _ =>
            attachmentMethod match {
              case Attached =>
                upscanService.deleteAllUpscanDetails(profile.registrationId).map { _ =>
                  Redirect(controllers.fileupload.routes.UploadDocumentController.show)
                }
              case Post =>
                Future.successful(Redirect(routes.DocumentsPostController.show))
              case _ =>
                Future.successful(BadRequest(view(form().fill(attachmentMethod))))
            }
          }
      }
    )
  }

}
