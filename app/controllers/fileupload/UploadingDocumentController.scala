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
import models.external.upscan.{Failed, InProgress, Ready}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.fileupload.UploadingDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadingDocumentController @Inject()(uploadingDocument: UploadingDocument,
                                            upscanService: UpscanService,
                                            val authConnector: AuthConnector,
                                            val sessionService: SessionService
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      Future.successful(Ok(uploadingDocument(request.session.get("reference").getOrElse(throw new InternalServerException("Upscan document reference not in session")))))
  }

  def poll(reference: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      upscanService.getUpscanFileStatus(profile.registrationId, reference).map { status =>
        Ok(Json.obj("status" -> Json.toJson(status)))
      }
  }

  def submit(reference: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request =>
    implicit profile =>
      upscanService.getUpscanFileStatus(profile.registrationId, reference).map {
        case InProgress => Redirect(routes.UploadingDocumentController.show)
        case Ready => Redirect(routes.DocumentUploadSummaryController.show)
        case Failed => Redirect(routes.UploadDocumentController.show)
      }
  }
}