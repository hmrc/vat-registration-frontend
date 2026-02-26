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
import utils.LoggingUtil
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
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport with LoggingUtil {

  def show: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    logger.info("[AttachmentMethodController][show]")
    val view = if (isEnabled(VrsNewAttachmentJourney)) viewNewAttachmentJourney.apply _ else viewOldJourney.apply _
    attachmentsService.getAttachmentDetails(profile.registrationId).map {
      case Some(Attachments(Some(method), _, _, _, _)) =>
        logger.info(s"[AttachmentMethodController][show] Loading form with $method method selected")
        Ok(view(form().fill(method)))
      case _ =>
        logger.info("[AttachmentMethodController][show] Loading empty form")
        Ok(view(form()))
    }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    val isNewJourney = isEnabled(VrsNewAttachmentJourney)
    val view = if (isNewJourney) viewNewAttachmentJourney.apply _ else viewOldJourney.apply _
    form().bindFromRequest().fold(
      formWithErrors => {
        logger.warn("[AttachmentMethodController][submit] Loading form with errors")
        Future.successful(BadRequest(view(formWithErrors)))
      },
      attachmentMethod => {
        if (isNewJourney  && attachmentMethod.equals(Post)) {
          upscanService.fetchAllUpscanDetails(profile.registrationId).flatMap { details =>
            if (details.exists(_.fileStatus.equals(InProgress))) {
              logger.error("[AttachmentMethodController][submit] Cannot post documents; there is an upload already in progress")
              Future.successful(Redirect(routes.DocumentsPostErrorController.show))
            }
            else { storeAttachmentDetails(profile, attachmentMethod) }
          }
        }
        else  { storeAttachmentDetails(profile, attachmentMethod) }
      }
    )
  }

  private def storeAttachmentDetails(profile: CurrentProfile, attachmentMethod: AttachmentMethod)(implicit request: Request[_]) = {
    logger.info("[AttachmentMethodController][storeAttachmentDetails] Storing attachment details - method " + attachmentMethod.toString)
    val isNewJourney = isEnabled(VrsNewAttachmentJourney)
    attachmentsService
      .storeAttachmentDetails(profile.registrationId, attachmentMethod)
      .flatMap { _ =>
        attachmentMethod match {
          case Upload =>
            if (isNewJourney) {
              Future.successful(Redirect(controllers.fileupload.routes.UploadSummaryController.show))
            }
            else {
              logger.info(s"[AttachmentMethodController][storeAttachmentDetails] Deleting upscan details for registration Id ${profile.registrationId}")
                upscanService.deleteAllUpscanDetails(profile.registrationId).flatMap {
                  _ => Future.successful(Redirect(controllers.fileupload.routes.UploadDocumentController.show))
                }
              }
          case Post =>
            logger.info("[AttachmentMethodController][storeAttachmentDetails] Redirecting to postal page")
            Future.successful(Redirect(if(isNewJourney) routes.PostalConfirmationController.show else routes.DocumentsPostController.show))
        }
      }
  }
}
