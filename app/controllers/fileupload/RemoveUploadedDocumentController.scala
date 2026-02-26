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
import forms.RemoveUploadedDocumentForm
import models.external.upscan.UpscanDetails
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException
import views.html.fileupload.RemoveUploadedDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveUploadedDocumentController @Inject()(val authConnector: AuthConnector,
                                                 val sessionService: SessionService,
                                                 upscanService: UpscanService,
                                                 view: RemoveUploadedDocument)
                                                (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show(reference: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        upscanService.fetchUpscanFileDetails(profile.registrationId, reference).flatMap {
          upscanDetails: UpscanDetails =>
            upscanDetails.uploadDetails match {
              case Some(uploadDetails) =>
                Future.successful(
                  Ok(view(
                    RemoveUploadedDocumentForm(uploadDetails.fileName).form, upscanDetails.reference, uploadDetails.fileName
                  ))
                )
              case None =>
                throw new InternalServerException("Invalid document reference for remove uploaded document page")
            }
        }.recover {
          case _ => Redirect(routes.DocumentUploadSummaryController.show)
        }
  }

  def submit(reference: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        upscanService.fetchUpscanFileDetails(profile.registrationId, reference).flatMap {
          upscanDetails: UpscanDetails =>
            upscanDetails.uploadDetails match {
              case Some(uploadDetails) =>
                RemoveUploadedDocumentForm(uploadDetails.fileName).form.bindFromRequest().fold(
                  errors => Future.successful(BadRequest(view(errors, reference, uploadDetails.fileName))),
                  success => {
                    if (success) {
                      for {
                        _ <- upscanService.deleteUpscanDetails(profile.registrationId, reference)
                      } yield Redirect(routes.DocumentUploadSummaryController.show)
                    } else {
                      Future.successful(Redirect(routes.DocumentUploadSummaryController.show))
                    }
                  }
                )
              case None =>
                throw new InternalServerException("Invalid document reference for remove uploaded document page")
            }
        }
  }
}
