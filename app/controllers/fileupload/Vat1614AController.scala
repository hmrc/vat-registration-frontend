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
import forms.Vat1614AForm
import models.api.{Attachment1614a, Attachment1614h}
import play.api.mvc.{Action, AnyContent}
import services.AttachmentsService.Supply1614AAnswer
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.Vat1614A

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Vat1614AController @Inject()(val authConnector: AuthConnector,
                                   val sessionService: SessionService,
                                   attachmentsService: AttachmentsService,
                                   upscanService: UpscanService,
                                   page: Vat1614A
                                  )(implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        attachmentsService.getAttachmentDetails(profile.registrationId).map { attachmentDetails =>
          attachmentDetails.flatMap(_.supplyVat1614a) match {
            case Some(answer) => Ok(page(Vat1614AForm.form.fill(answer)))
            case _ => Ok(page(Vat1614AForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        Vat1614AForm.form.bindFromRequest().fold(
          errors => Future.successful(BadRequest(page(errors))),
          success => {
            attachmentsService.storeAttachmentDetails(profile.registrationId, Supply1614AAnswer(success)).flatMap { _ =>
              if (success) {
                upscanService.deleteUpscanDetailsByType(profile.registrationId, Attachment1614h).map { _ =>
                  Redirect(routes.UploadVat1614AController.show)
                }
              } else {
                upscanService.deleteUpscanDetailsByType(profile.registrationId, Attachment1614a).map { _ =>
                  Redirect(routes.Vat1614HController.show)
                }
              }
            }
          }
        )
  }

}
