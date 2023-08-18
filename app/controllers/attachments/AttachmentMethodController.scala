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

package controllers.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import forms.AttachmentMethodForm
import models.CurrentProfile
import models.api._
import models.external.upscan.InProgress
import play.api.mvc.{Action, AnyContent, Request}
import services.{AttachmentsService, SessionProfile, SessionService, UpscanService}
import views.html.attachments.{ChooseAttachmentMethod, ChooseAttachmentMethodNewJourney}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachmentMethodController @Inject()(val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           attachmentsService: AttachmentsService,
                                           upscanService: UpscanService,
                                           form: AttachmentMethodForm,
                                           viewOldJourney: ChooseAttachmentMethod,
                                           viewNewAttachmentJourney: ChooseAttachmentMethodNewJourney)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile with FeatureToggleSupport {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    val view = if (isEnabled(VrsNewAttachmentJourney)) viewNewAttachmentJourney.apply _ else viewOldJourney.apply _
    attachmentsService.getAttachmentDetails(profile.registrationId).map {
      case Some(Attachments(Some(method), _, _, _, _, _)) =>
        Ok(view(form().fill(method)))
      case _ =>
        Ok(view(form()))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    val isNewJourney = isEnabled(VrsNewAttachmentJourney)
    val view = if (isNewJourney) viewNewAttachmentJourney.apply _ else viewOldJourney.apply _
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors))),
      attachmentMethod => {
        if (isNewJourney  && attachmentMethod.equals(Post)) {
          upscanService.fetchAllUpscanDetails(profile.registrationId).flatMap { details =>
            if (details.exists(_.fileStatus.equals(InProgress))) { Future.successful(Redirect(routes.DocumentsPostErrorController.show)) }
            else { storeAttachmentDetails(profile, attachmentMethod) }
          }
        }
        else  { storeAttachmentDetails(profile, attachmentMethod) }
      }
    )
  }

  private def storeAttachmentDetails(profile: CurrentProfile, attachmentMethod: AttachmentMethod)(implicit request: Request[_]) = {
    val isNewJourney = isEnabled(VrsNewAttachmentJourney)
    attachmentsService
      .storeAttachmentDetails(profile.registrationId, attachmentMethod)
      .flatMap { _ =>
        attachmentMethod match {
          case Upload =>
            upscanService.deleteAllUpscanDetails(profile.registrationId).map { _ =>
              Redirect(controllers.fileupload.routes.UploadDocumentController.show)
            }
          case Post =>
            Future.successful(Redirect(if(isNewJourney) routes.PostalConfirmationController.show else routes.DocumentsPostController.show))
        }
      }
  }
}
