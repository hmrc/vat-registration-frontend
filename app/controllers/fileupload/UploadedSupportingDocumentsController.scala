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
import controllers.fileupload.SupportingDocumentUploadController.{maxSupportingDocuments, uploadedSupportingDocuments}
import forms.UploadedSupportingDocumentsForm
import models.external.upscan.UpscanDetails
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.UploadedSupportingDocuments

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadedSupportingDocumentsController @Inject()(view: UploadedSupportingDocuments,
                                                      upscanService: UpscanService,
                                                      val authConnector: AuthConnector,
                                                      val sessionService: SessionService
                                                     )(implicit appConfig: FrontendAppConfig,
                                                       val executionContext: ExecutionContext,
                                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchAllUpscanDetails(profile.registrationId).map { upscanDetails =>
        val documents = uploadedSupportingDocuments(upscanDetails)
        if (documents.isEmpty) {
          Redirect(routes.SupportingDocumentUploadController.show)
        } else {
          Ok(view(UploadedSupportingDocumentsForm.form, documents.map(asSummaryRow), documents.size < maxSupportingDocuments))
        }
      }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchAllUpscanDetails(profile.registrationId).map { upscanDetails =>
        val documents = uploadedSupportingDocuments(upscanDetails)
        if (documents.isEmpty) {
          Redirect(routes.SupportingDocumentUploadController.show)
        } else if (documents.size >= maxSupportingDocuments) {
          Redirect(routes.VAT1614AController.show)
        } else {
          UploadedSupportingDocumentsForm.form.bindFromRequest().fold(
            errors => BadRequest(view(errors, documents.map(asSummaryRow), canAddAnother = true)),
            addAnother =>
              if (addAnother) Redirect(routes.SupportingDocumentUploadController.show)
              else Redirect(routes.VAT1614AController.show)
          )
        }
      }
  }

  private def asSummaryRow(details: UpscanDetails): DocumentUploadSummaryRow = {
    val fileName = details.uploadDetails.map(_.fileName).getOrElse(
      throw new InternalServerException(s"Failed to render uploaded supporting documents page, missing file name for reference: ${details.reference}")
    )
    DocumentUploadSummaryRow(fileName, routes.RemoveSupportingDocumentController.show(details.reference))
  }
}
