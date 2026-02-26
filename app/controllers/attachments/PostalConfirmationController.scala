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
import forms.PostalConfirmationPageForm
import play.api.mvc.{Action, AnyContent}
import services._
import views.html.attachments.PostalConfirmation

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.api.{Post, Upload}
import utils.LoggingUtil

@Singleton
class PostalConfirmationController @Inject()(val authConnector: AuthClientConnector,
                                             val sessionService: SessionService,
                                             val attachmentsService: AttachmentsService,
                                             val upscanService: UpscanService,
                                             postalConfirmationPage: PostalConfirmation)
                                            (implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with LoggingUtil {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      _ =>
        logger.info("[PostalConfirmationController][show] Loading postal confirmation page")
        Future.successful(Ok(postalConfirmationPage(PostalConfirmationPageForm())))
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile => {
    PostalConfirmationPageForm().bindFromRequest().fold(
      formWithErrors => {
        logger.warn("[PostalConfirmationController][submit] No answer selected")
        Future.successful(BadRequest(postalConfirmationPage(formWithErrors)))
      },
      {
        answer => {
          attachmentsService.storeAttachmentDetails(profile.registrationId, if(answer) Post else Upload)
          if (answer) {
            logger.info("[PostalConfirmationController][submit] Yes selected; redirecting to Post Documents page")
            upscanService.deleteAllUpscanDetails(profile.registrationId)
              .flatMap(_ => Future.successful(Redirect(routes.DocumentsPostController.show)))

          }
          else {
            logger.info("[PostalConfirmationController][submit] No selected; redirecting to Upload task list")
            Future.successful(Redirect(controllers.routes.TaskListController.show)) // TODO: placeholder; redirect to correct page when DL-10922 completed
          }
        }
      })
  }}

}
