/*
 * Copyright 2023 HM Revenue & Customs
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
import models.api._
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.UploadDocumentHintBuilder
import views.html.fileupload.UploadDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadOptionToTaxDocumentController @Inject()(view: UploadDocument,
                                                    upscanService: UpscanService,
                                                    attachmentsService: AttachmentsService,
                                                    uploadDocumentHint: UploadDocumentHintBuilder,
                                                    val authConnector: AuthConnector,
                                                    val sessionService: SessionService
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request =>
    implicit profile =>
      attachmentsService.getAttachmentDetails(profile.registrationId).flatMap {
        case Some(Attachments(Some(Upload), supplyVat1614a, supplyVat1614h, _, _, _)) if List(supplyVat1614a, supplyVat1614h).flatten.contains(true) =>
          val attachmentType = if (supplyVat1614a.contains(true)) {
            Attachment1614a
          } else {
            Attachment1614h
          }

          for {
            _ <- upscanService.deleteUpscanDetailsByType(profile.registrationId, attachmentType)
            upscanResponse <- upscanService.initiateUpscan(profile.registrationId, attachmentType)
            hintHtml <- uploadDocumentHint.build(attachmentType)
          } yield {
            val optErrorCode = request.queryString.get("errorCode").flatMap(_.headOption)
            Ok(view(upscanResponse, Some(hintHtml), attachmentType, optErrorCode)).addingToSession("reference" -> upscanResponse.reference)
          }
        case _ =>
          Future.successful(Redirect(routes.DocumentUploadSummaryController.show))
      }
  }
}