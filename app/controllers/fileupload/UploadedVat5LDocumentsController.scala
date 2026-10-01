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
import models.api.{Attachment1614a, Attachment1614h, AttachmentType, LandPropertyOtherDocs, VAT5L}
import models.external.upscan.{Ready, UpscanDetails}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import viewmodels.DocumentUploadSummaryRow
import views.html.fileupload.UploadedVat5LDocuments

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadedVat5LDocumentsController @Inject()(val authConnector: AuthConnector,
                                                  val sessionService: SessionService,
                                                  upscanService: UpscanService,
                                                  view: UploadedVat5LDocuments
                                                 )(implicit appConfig: FrontendAppConfig,
                                                   val executionContext: ExecutionContext,
                                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  private val vat5LDocumentTypes = Set[AttachmentType](VAT5L, LandPropertyOtherDocs, Attachment1614a, Attachment1614h)

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      upscanService.fetchAllUpscanDetails(profile.registrationId).map { allDetails =>
        Ok(view(scanDetailsAsSummaryRows(allDetails)))
      }
  }

  val continue: Action[AnyContent] = isAuthenticatedWithProfile { _ =>
    _ =>
      Future.successful(Redirect(routes.UploadSummaryController.show))
  }

  private def scanDetailsAsSummaryRows(upscanDetails: Seq[UpscanDetails]): Seq[DocumentUploadSummaryRow] =
    upscanDetails.filter(details => vat5LDocumentTypes.contains(details.attachmentType) && details.fileStatus.equals(Ready)).map { details =>
      val fileName = details.uploadDetails.map(_.fileName).getOrElse(
        throw new InternalServerException(s"Failed to render uploaded VAT5L documents page, missing file name for reference: ${details.reference}")
      )
      DocumentUploadSummaryRow(fileName, routes.RemoveUploadedDocumentController.show(details.reference))
    }

}
