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
import models.api.{Attached, Attachments, LandPropertyOtherDocs}
import play.api.mvc.{Action, AnyContent}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.UploadDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadSupportingDocumentController @Inject()(view: UploadDocument,
                                                   upscanService: UpscanService,
                                                   attachmentsService: AttachmentsService,
                                                   val authConnector: AuthConnector,
                                                   val sessionService: SessionService
                                                  )(implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    attachmentsService.getAttachmentDetails(profile.registrationId).flatMap {
      case Some(Attachments(Some(Attached), _, _, Some(true))) =>
        upscanService.fetchAllUpscanDetails(profile.registrationId).flatMap {
          case list if list.count(_.attachmentType.equals(LandPropertyOtherDocs)) < 20 =>
            upscanService.initiateUpscan(profile.registrationId, LandPropertyOtherDocs).map { upscanResponse =>
              val optErrorCode = request.queryString.get("errorCode").flatMap(_.headOption)
              Ok(view(upscanResponse, None, LandPropertyOtherDocs, optErrorCode)).addingToSession("reference" -> upscanResponse.reference)
            }
          case _ =>
            Future.successful(Redirect(routes.DocumentUploadSummaryController.show))
        }
      case _ =>
        Future.successful(Redirect(routes.DocumentUploadSummaryController.show))
    }
  }
}