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
import controllers.fileupload.SupportingDocumentUploadController.maxSupportingDocuments
import models.api.{Attachments, LandPropertyOtherDocs, Upload}
import models.external.upscan.{Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.SupportingDocumentUpload

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SupportingDocumentUploadController @Inject()(view: SupportingDocumentUpload,
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
      attachmentsService.getAttachmentDetails(profile.registrationId).flatMap {
        case Some(Attachments(Some(Upload), _, _, Some(true), _)) =>
          for {
            upscanDetails <- upscanService.fetchAllUpscanDetails(profile.registrationId)
            uploadedDocuments = uploadedSupportingDocuments(upscanDetails)
            optUpscanResponse <-
              if (uploadedDocuments.size < maxSupportingDocuments) {
                upscanService.initiateUpscan(
                  profile.registrationId,
                  LandPropertyOtherDocs,
                  errorRedirectUrl = routes.SupportingDocumentUploadController.show.url,
                  successRedirectUrl = routes.UploadingVat5LDocumentController.show.url
                ).map(Some(_))
              } else {
                Future.successful(None)
              }
          } yield {
            val optErrorCode = request.queryString.get("errorCode").flatMap(_.headOption)
            val page = Ok(view(uploadedDocuments.map(asSummaryRow), optUpscanResponse, optErrorCode))
            optUpscanResponse.fold(page)(upscanResponse => page.addingToSession("reference" -> upscanResponse.reference))
          }
        case Some(Attachments(None, _, _, _, _)) | None =>
          Future.successful(Redirect(controllers.attachments.routes.AttachmentMethodController.show))
        case _ =>
          Future.successful(Redirect(routes.VAT1614AController.show))
      }
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchAllUpscanDetails(profile.registrationId).map { upscanDetails =>
        if (uploadedSupportingDocuments(upscanDetails).isEmpty) {
          Redirect(routes.SupportingDocumentUploadController.show.url, Map("errorCode" -> Seq("InvalidArgument")))
        } else {
          Redirect(routes.VAT1614AController.show)
        }
      }
  }

  private def uploadedSupportingDocuments(upscanDetails: Seq[UpscanDetails]): Seq[UpscanDetails] =
    upscanDetails.filter(details => details.attachmentType == LandPropertyOtherDocs && details.fileStatus == Ready)

  private def asSummaryRow(details: UpscanDetails): DocumentUploadSummaryRow = {
    val fileName = details.uploadDetails.map(_.fileName).getOrElse(
      throw new InternalServerException(s"Failed to render supporting document upload page, missing file name for reference: ${details.reference}")
    )
    DocumentUploadSummaryRow(fileName, routes.RemoveSupportingDocumentController.show(details.reference))
  }
}

object SupportingDocumentUploadController {
  val maxSupportingDocuments = 10
}
