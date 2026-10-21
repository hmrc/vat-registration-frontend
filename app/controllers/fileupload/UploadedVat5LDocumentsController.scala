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
import controllers.fileupload.UploadedVat5LDocumentsController.vat5LAttachmentTypes
import models.api._
import models.external.upscan.{InProgress, Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.UploadedVat5LDocumentRow
import views.html.fileupload.UploadedVat5LDocuments

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadedVat5LDocumentsController @Inject()(view: UploadedVat5LDocuments,
                                                 upscanService: UpscanService,
                                                 attachmentsService: AttachmentsService,
                                                 val authConnector: AuthConnector,
                                                 val sessionService: SessionService
                                                )(implicit appConfig: FrontendAppConfig,
                                                  val executionContext: ExecutionContext,
                                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      for {
        upscanResponse <- upscanService.fetchAllUpscanDetails(profile.registrationId)
        uploadSummaryRows = vat5LDocumentRows(upscanResponse.filter(details => vat5LAttachmentTypes.contains(details.attachmentType) && details.fileStatus != InProgress))
        incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
      } yield {
        uploadSummaryRows match {
          case Nil =>
            Redirect(routes.UploadDocumentController.show)
          case _ =>
            Ok(view(uploadSummaryRows, incompleteAttachments.size))
        }
      }
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      attachmentsService.getIncompleteAttachments(profile.registrationId).map { incompleteAttachments =>
        if (incompleteAttachments.nonEmpty) {
          Redirect(routes.UploadDocumentController.show)
        } else {
          Redirect(controllers.routes.TaskListController.show.url)
        }
      }
  }

  private def vat5LDocumentRows(upscanResponse: Seq[UpscanDetails]): Seq[UploadedVat5LDocumentRow] = {
    upscanResponse.map { details =>
      val fileName = details.uploadDetails.map(_.fileName)
      if (details.fileStatus == Ready && fileName.isEmpty) {
        throw new InternalServerException(s"Failed to render upscan summary page, missing file name for reference: ${details.reference}")
      }
      val removeAction = if (details.fileStatus == Ready) Some(routes.RemoveVat5LDocumentController.show(details.reference)) else None
      UploadedVat5LDocumentRow(details.attachmentType, fileName, details.fileStatus, removeAction)
    }
  }
}

object UploadedVat5LDocumentsController {
  val vat5LAttachmentTypes: Seq[AttachmentType] = Seq(VAT5L, LandPropertyOtherDocs, Attachment1614a, Attachment1614h)
}
