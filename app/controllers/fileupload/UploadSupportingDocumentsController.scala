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

package controllers.fileupload

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.fileupload.DocumentUploadSummaryController.maxSupportingLandAndPropertyDocs
import models.api.{Attachments, LandPropertyOtherDocs, Upload}
import models.external.upscan.{Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.UploadSupportingDocuments

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadSupportingDocumentsController @Inject()(val authConnector: AuthConnector,
                                                     val sessionService: SessionService,
                                                     attachmentsService: AttachmentsService,
                                                     upscanService: UpscanService,
                                                     view: UploadSupportingDocuments
                                                    )(implicit appConfig: FrontendAppConfig,
                                                      val executionContext: ExecutionContext,
                                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      attachmentsService.getAttachmentDetails(profile.registrationId).flatMap {
        case Some(Attachments(Some(Upload), _, _, Some(true), _)) =>
          upscanService.fetchAllUpscanDetails(profile.registrationId).flatMap { allDetails =>
            val uploadedRows = scanDetailsAsSummaryRows(allDetails)
            val uploadedCount = allDetails.count(_.attachmentType.equals(LandPropertyOtherDocs))

            if (uploadedCount < maxSupportingLandAndPropertyDocs) {
              upscanService.initiateUpscan(profile.registrationId, LandPropertyOtherDocs).map { upscanResponse =>
                Ok(view(Some(upscanResponse), uploadedRows))
              }
            } else {
              Future.successful(Ok(view(None, uploadedRows)))
            }
          }
        case _ =>
          Future.successful(Redirect(controllers.routes.TaskListController.show.url))
      }
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile { _ =>
    _ =>
      Future.successful(Redirect(routes.Vat1614AController.show))
  }

  private def scanDetailsAsSummaryRows(upscanDetails: Seq[UpscanDetails]): Seq[DocumentUploadSummaryRow] =
    upscanDetails.filter(details => details.attachmentType.equals(LandPropertyOtherDocs) && details.fileStatus.equals(Ready)).map { details =>
      val fileName = details.uploadDetails.map(_.fileName).getOrElse(
        throw new InternalServerException(s"Failed to render upload supporting documents page, missing file name for reference: ${details.reference}")
      )
      DocumentUploadSummaryRow(fileName, routes.RemoveUploadedDocumentController.show(details.reference))
    }

}
