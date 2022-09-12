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

package controllers.fileupload

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaskList
import forms.SupplySupportingDocumentsForm
import models.api.LandPropertyOtherDocs
import play.api.mvc.{Action, AnyContent}
import services.AttachmentsService.SupplySupportingDocumentsAnswer
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.SupplySupportingDocuments

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SupplySupportingDocumentsController @Inject()(val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    attachmentsService: AttachmentsService,
                                                    upscanService: UpscanService,
                                                    page: SupplySupportingDocuments
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        attachmentsService.getAttachmentDetails(profile.registrationId).map { attachmentDetails =>
          attachmentDetails.flatMap(_.supplySupportingDocuments) match {
            case Some(answer) => Ok(page(SupplySupportingDocumentsForm.form.fill(answer)))
            case _ => Ok(page(SupplySupportingDocumentsForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        SupplySupportingDocumentsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(page(errors))),
          success => {
            attachmentsService.storeAttachmentDetails(profile.registrationId, SupplySupportingDocumentsAnswer(success)).flatMap { _ =>
              if (success) {
                Future.successful(NotImplemented)
              } else {
                upscanService.deleteUpscanDetailsByType(profile.registrationId, LandPropertyOtherDocs).map { _ =>
                  if (isEnabled(TaskList)) {
                    Redirect(controllers.routes.TaskListController.show.url)
                  } else {
                    Redirect(controllers.routes.SummaryController.show.url)
                  }
                }
              }
            }
          }
        )
  }

}
