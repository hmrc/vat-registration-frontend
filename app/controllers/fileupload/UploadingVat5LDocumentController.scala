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
import models.api.{Attachment1614a, Attachment1614h, LandPropertyOtherDocs, VAT5L}
import models.external.upscan.{Failed, FailureDetails, InProgress, Ready, UpscanDetails}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.fileupload.UploadingVat5LDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadingVat5LDocumentController @Inject()(uploadingDocument: UploadingVat5LDocument,
                                            upscanService: UpscanService,
                                            val authConnector: AuthConnector,
                                            val sessionService: SessionService
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      val reference = request.session.get("reference").getOrElse(throw new InternalServerException("Upscan document reference not in session"))
      upscanService.fetchUpscanFileDetails(profile.registrationId, reference).map{ upscanDetails =>
        Ok(uploadingDocument(upscanDetails))
      }
  }

  def poll(reference: String): Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchUpscanFileDetails(profile.registrationId, reference).map { details =>
        Ok(
          Json.obj("status" -> Json.toJson(details.fileStatus)) ++
            details.failureDetails.fold(Json.obj())(failure => Json.obj("reason" -> failure.failureReason))
        )
      }
  }

  def submit(reference: String): Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchUpscanFileDetails(profile.registrationId, reference).flatMap { details =>
        details.fileStatus match {
          case InProgress => Future.successful(Redirect(routes.UploadingVat5LDocumentController.show))
          case Ready =>
            details.attachmentType match {
              case VAT5L => Future.successful(Redirect(routes.SupportingDocumentsController.show))
              case LandPropertyOtherDocs => Future.successful(Redirect(routes.SupportingDocumentUploadController.show))
              case Attachment1614a => Future.successful(Redirect(routes.VAT1614HController.show))
              case Attachment1614h => Future.successful(Redirect(routes.UploadedVat5LDocumentsController.show))
              case _ => Future.successful(Redirect(routes.UploadedVat5LDocumentsController.show))
            }
          case Failed if details.attachmentType == LandPropertyOtherDocs =>
            upscanService.deleteUpscanDetails(profile.registrationId, reference).map { _ =>
              Redirect(routes.SupportingDocumentUploadController.show.url, Map("errorCode" -> Seq(errorCode(details))))
            }
          case Failed =>
            if (details.failureDetails.exists(_.failureReason.equals(FailureDetails.rejectedKey))) {
              Future.successful(Redirect(routes.DocumentUploadTypeErrorController.show))
            } else {
              Future.successful(Redirect(routes.DocumentUploadErrorController.show))
            }
        }
      }
  }

  private def errorCode(details: UpscanDetails): String =
    details.failureDetails.map(_.failureReason) match {
      case Some(FailureDetails.rejectedKey) => "InvalidFileType"
      case Some(FailureDetails.quarantineKey) => "Quarantine"
      case _ => "Unknown"
    }
}
