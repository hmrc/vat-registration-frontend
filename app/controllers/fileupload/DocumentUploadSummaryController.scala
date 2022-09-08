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
import featureswitch.core.config.OptionToTax
import models.api.VAT5L
import models.external.upscan.{Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.DocumentUploadSummary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

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

  def show(): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    profile =>
      for {
        upscanResponse <- upscanService.fetchAllUpscanDetails(profile.registrationId)
        uploadSummaryRows = scanDetailsAsSummaryRows(upscanResponse)
        incompleteAttachments <- attachmentsService.getIncompleteAttachments(profile.registrationId)
        needOptionToTax = isEnabled(OptionToTax) && upscanResponse.map(_.attachmentType).contains(VAT5L) && incompleteAttachments.isEmpty
      } yield {
        uploadSummaryRows match {
          case Nil => Redirect(routes.UploadDocumentController.show)
          case _ => Ok(view(uploadSummaryRows, incompleteAttachments.size, needOptionToTax))
        }
      }
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