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

package controllers.registration.otherbusinessinvolvements

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.otherbusinessinvolvements.{HasUtrForm, HaveVatNumberForm}
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.OtherBusinessInvolvementsService.HasUtrAnswer
import services.{OtherBusinessInvolvementsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.otherbusinessinvolvements.HasUtr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HasUtrController @Inject()(val authConnector: AuthConnector,
                                  val sessionService: SessionService,
                                  otherBusinessInvolvementsService: OtherBusinessInvolvementsService,
                                  view: HasUtr)
                                 (implicit appConfig: FrontendAppConfig,
                                  val executionContext: ExecutionContext,
                                  baseControllerComponents: BaseControllerComponents)
  extends BaseController with ObiIndexValidation with SessionProfile {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.HasUtrController.show) {
          otherBusinessInvolvementsService.getOtherBusinessInvolvement(index).flatMap {
            case Some(OtherBusinessInvolvement(_, _, _, optHasUtr, _, _)) =>
              Future.successful(Ok(view(optHasUtr.fold(HasUtrForm())(HasUtrForm().fill(_)), index)))
            case None =>
              otherBusinessInvolvementsService.getHighestValidIndex.map { maxIndex =>
                if (index > maxIndex) {
                  Redirect(routes.HasUtrController.show(maxIndex))
                } else {
                  Ok(view(HaveVatNumberForm(), index))
                }
              }
          }
        }
  }

  def submit(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        HasUtrForm().bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors, index))),
          hasUtr => {
            otherBusinessInvolvementsService.updateOtherBusinessInvolvement(index, HasUtrAnswer(hasUtr)).map { _ =>
              if (hasUtr) {
                Redirect(routes.CaptureUtrController.show(index))
              } else {
                Redirect(routes.OtherBusinessActivelyTradingController.show(index))
              }
            }
          }
        )
  }
}
