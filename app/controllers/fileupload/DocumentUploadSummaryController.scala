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
import forms.DocumentUploadSummaryForm
import models.api._
import models.external.upscan.{Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.DocumentUploadSummary

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocumentUploadSummaryController @Inject()(view: DocumentUploadSummary,
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
        uploadSummaryRows = scanDetailsAsSummaryRows(upscanResponse)
        incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
        attachmentDetails <- attachmentsService.getAttachmentDetails(profile.registrationId).map(_.getOrElse(Attachments()))
        uploadedAttachments = upscanResponse.map(_.attachmentType)
        showSupplySupportingDocuments = incompleteAttachments.isEmpty &&
          canSupply1614Form(uploadedAttachments) &&
          is1614FormComplete(attachmentDetails, uploadedAttachments) &&
          attachmentDetails.supplySupportingDocuments.contains(true) &&
          1 <= uploadedAttachments.count(_.equals(LandPropertyOtherDocs)) &&
          uploadedAttachments.count(_.equals(LandPropertyOtherDocs)) < maxSupportingLandAndPropertyDocs
      } yield {
        uploadSummaryRows match {
          case Nil =>
            Redirect(routes.UploadDocumentController.show)
          case _ =>
            Ok(view(DocumentUploadSummaryForm.form, uploadSummaryRows, incompleteAttachments.size, showSupplySupportingDocuments))
        }
      }
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      for {
        upscanResponse <- upscanService.fetchAllUpscanDetails(profile.registrationId)
        incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
        attachmentDetails <- attachmentsService.getAttachmentDetails(profile.registrationId).map(_.getOrElse(Attachments()))
        uploadedAttachments = upscanResponse.map(_.attachmentType)
      } yield {
        if (incompleteAttachments.nonEmpty) {
          Redirect(routes.UploadDocumentController.show)
        } else if (canSupply1614Form(uploadedAttachments) && !is1614FormComplete(attachmentDetails, uploadedAttachments)) {
          Redirect(routes.Supply1614AController.show)
        } else if (canSupply1614Form(uploadedAttachments) && !areSupportingDocumentsComplete(attachmentDetails, uploadedAttachments)) {
          Redirect(routes.SupplySupportingDocumentsController.show)
        } else {
          Redirect(controllers.routes.TaskListController.show.url)
        }
      }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      DocumentUploadSummaryForm.form.bindFromRequest().fold(
        errors => for {
          upscanResponse <- upscanService.fetchAllUpscanDetails(profile.registrationId)
          uploadSummaryRows = scanDetailsAsSummaryRows(upscanResponse)
          incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
        } yield BadRequest(view(errors, uploadSummaryRows, incompleteAttachments.size, supplySupportingDocuments = true)),
        success => {
          if (success) {
            Future.successful(Redirect(routes.UploadSupportingDocumentController.show.url))
          } else {
            Future.successful(Redirect(controllers.routes.TaskListController.show.url))
          }
        }
      )
  }

  private def canSupply1614Form(uploadedAttachments: Seq[AttachmentType]): Boolean = uploadedAttachments.contains(VAT5L)

  private def is1614FormComplete(attachmentDetails: Attachments, uploadedAttachments: Seq[AttachmentType]): Boolean = attachmentDetails match {
    case Attachments(_, Some(false), Some(false), _, _) => true
    case Attachments(_, Some(false), Some(true), _, _) if uploadedAttachments.contains(Attachment1614h) => true
    case Attachments(_, Some(true), _, _, _) if uploadedAttachments.contains(Attachment1614a) => true
    case _ => false
  }

  private def areSupportingDocumentsComplete(attachmentDetails: Attachments, uploadedAttachments: Seq[AttachmentType]): Boolean = attachmentDetails match {
    case Attachments(_, _, _, Some(false), _) => true
    case Attachments(_, _, _, Some(true), _) if uploadedAttachments.contains(LandPropertyOtherDocs) => true
    case _ => false
  }

  private def scanDetailsAsSummaryRows(upscanResponse: Seq[UpscanDetails]): Seq[DocumentUploadSummaryRow] = {
    upscanResponse.filter(_.fileStatus == Ready).map(details => {
      val fileName = details.uploadDetails.map(_.fileName).getOrElse(
        throw new InternalServerException(s"Failed to render upscan summary page, missing file name for reference: ${details.reference}")
      )
      DocumentUploadSummaryRow(fileName, routes.RemoveUploadedDocumentController.show(details.reference))
    })
  }
}

object DocumentUploadSummaryController {
  val maxSupportingLandAndPropertyDocs = 20
}
