/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.Supply1614HForm
import models.api.{Attachment1614a, Attachment1614h, LandPropertyOtherDocs}
import models.external.upscan.Ready
import play.api.mvc.{Action, AnyContent}
import services.AttachmentsService.Supply1614HAnswer
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.Supply1614H

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Supply1614HController @Inject()(val authConnector: AuthConnector,
                                      val sessionService: SessionService,
                                      attachmentsService: AttachmentsService,
                                      upscanService: UpscanService,
                                      page: Supply1614H
                                     )(implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        attachmentsService.getAttachmentDetails(profile.registrationId).map { attachmentDetails =>
          attachmentDetails.flatMap(_.supplyVat1614h) match {
            case Some(answer) => Ok(page(Supply1614HForm.form.fill(answer)))
            case _ => Ok(page(Supply1614HForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Supply1614HForm.form.bindFromRequest().fold(
          errors => Future.successful(BadRequest(page(errors))),
          success => {
            attachmentsService.storeAttachmentDetails(profile.registrationId, Supply1614HAnswer(success)).flatMap { _ =>
              if (success) {
                upscanService.deleteUpscanDetailsByType(profile.registrationId, Attachment1614a).map { _ =>
                  Redirect(routes.UploadOptionToTaxDocumentController.show)
                }
              } else {
                upscanService.deleteUpscanDetailsByType(profile.registrationId, Attachment1614h).flatMap { _ =>
                  upscanService.fetchAllUpscanDetails(profile.registrationId).map { list =>
                    list.map(file => (file.attachmentType, file.fileStatus)) match {
                      case list if list.contains((LandPropertyOtherDocs, Ready)) =>
                        Redirect(routes.DocumentUploadSummaryController.show)
                      case _ =>
                        Redirect(routes.SupplySupportingDocumentsController.show)
                    }
                  }
                }
              }
            }
          }
        )
  }

}
