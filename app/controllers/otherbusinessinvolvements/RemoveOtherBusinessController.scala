/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.otherbusinessinvolvements

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.otherbusinessinvolvements.RemoveOtherBusinessForm
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.{OtherBusinessInvolvementsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.otherbusinessinvolvements.RemoveOtherBusiness

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveOtherBusinessController @Inject()(val authConnector: AuthConnector,
                                              val sessionService: SessionService,
                                              otherBusinessInvolvementsService: OtherBusinessInvolvementsService,
                                              view: RemoveOtherBusiness)
                                             (implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with ObiIndexValidation with SessionProfile {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.RemoveOtherBusinessController.show) {
          otherBusinessInvolvementsService.getOtherBusinessInvolvement(index).flatMap {
            case Some(OtherBusinessInvolvement(Some(businessName), _, _, _, _, _)) =>
              Future.successful(Ok(view(RemoveOtherBusinessForm(businessName).form, businessName, index)))
            case _ =>
              logger.warn("[RemoveOtherBusinessController] Attempted to remove OBI without OBI details")
              Future.successful(Redirect(routes.ObiSummaryController.show))
          }
        }
  }

  def submit(otherBusinessName: String, index: Int): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        RemoveOtherBusinessForm(otherBusinessName).form.bindFromRequest().fold(
          errors =>
            Future.successful(BadRequest(view(errors, otherBusinessName, index))),
          success => {
            if (success) {
              for {
                _ <- otherBusinessInvolvementsService.deleteOtherBusinessInvolvement(index)
                otherBusinessInvolvements <- otherBusinessInvolvementsService.getOtherBusinessInvolvements
              } yield if (otherBusinessInvolvements.nonEmpty) {
                Redirect(routes.ObiSummaryController.show)
              } else {
                Redirect(routes.OtherBusinessInvolvementController.show)
              }
            } else {
              Future.successful(Redirect(routes.ObiSummaryController.show))
            }
          }
        )
  }
}
