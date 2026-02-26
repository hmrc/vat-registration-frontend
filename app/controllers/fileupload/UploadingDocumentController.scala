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
import models.external.upscan.{Failed, FailureDetails, InProgress, Ready}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.fileupload.UploadingDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UploadingDocumentController @Inject()(uploadingDocument: UploadingDocument,
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
      upscanService.fetchUpscanFileDetails(profile.registrationId, reference).map { details =>
        details.fileStatus match {
          case InProgress => Redirect(routes.UploadingDocumentController.show)
          case Ready => Redirect(routes.DocumentUploadSummaryController.show)
          case Failed =>
            if (details.failureDetails.exists(_.failureReason.equals(FailureDetails.rejectedKey))) {
              Redirect(routes.DocumentUploadTypeErrorController.show)
            } else {
              Redirect(routes.DocumentUploadErrorController.show)
            }
        }
      }
  }
}