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
import models.api.{Attachment1614a, Attachments, Upload}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.VAT1614AUpload

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VAT1614AUploadController @Inject()(view: VAT1614AUpload,
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
        case Some(Attachments(Some(Upload), Some(true), _, _, _)) =>
          for {
            _ <- upscanService.deleteUpscanDetailsByType(profile.registrationId, Attachment1614a)
            upscanResponse <- upscanService.initiateUpscan(
              profile.registrationId,
              Attachment1614a,
              errorRedirectUrl = routes.VAT1614AUploadController.show.url,
              successRedirectUrl = routes.UploadingVat5LDocumentController.show.url
            )
          } yield {
            val optErrorCode = request.queryString.get("errorCode").flatMap(_.headOption)
            Ok(view(upscanResponse, optErrorCode)).addingToSession("reference" -> upscanResponse.reference)
          }
        case Some(Attachments(None, _, _, _, _)) | None =>
          Future.successful(Redirect(controllers.attachments.routes.AttachmentMethodController.show))
        case _ =>
          Future.successful(Redirect(routes.UploadedVat5LDocumentsController.show))
      }
  }
}
