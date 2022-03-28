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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.registration.otherbusinessinvolvements.{routes => obiRoutes}
import forms.otherbusinessinvolvements.OtherBusinessActivelyTradingForm
import models.OtherBusinessInvolvement
import play.api.mvc.{Action, AnyContent}
import services.OtherBusinessInvolvementsService.StillTradingAnswer
import services.{OtherBusinessInvolvementsService, SessionProfile, SessionService}
import views.html.otherbusinessinvolvements.OtherBusinessActivelyTradingView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherBusinessActivelyTradingController @Inject()(val sessionService: SessionService,
                                                       val authConnector: AuthClientConnector,
                                                       val otherBusinessInvolvementsService: OtherBusinessInvolvementsService,
                                                       view: OtherBusinessActivelyTradingView)
                                                      (implicit appConfig: FrontendAppConfig,
                                                       val executionContext: ExecutionContext,
                                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with ObiIndexValidation with SessionProfile {

  def show(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.OtherBusinessActivelyTradingController.show) {
          otherBusinessInvolvementsService.getOtherBusinessInvolvement(index).flatMap {
            case Some(OtherBusinessInvolvement(_, _, _, _, _, optStillTrading)) =>
              val form = OtherBusinessActivelyTradingForm.form
              Future.successful(Ok(view(optStillTrading.fold(form)(stillTrading => form.fill(stillTrading)), index)))
            case None =>
              otherBusinessInvolvementsService.getHighestValidIndex.map { maxIndex =>
                if (index > maxIndex) {
                  Redirect(routes.OtherBusinessActivelyTradingController.show(maxIndex))
                } else {
                  Ok(view(OtherBusinessActivelyTradingForm.form, index))
                }
              }
          }
        }
  }

  def submit(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        OtherBusinessActivelyTradingForm.form.bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors, index))),
          success =>
            otherBusinessInvolvementsService.updateOtherBusinessInvolvement(index, StillTradingAnswer(success)).map { _ =>
              Redirect(obiRoutes.OtherBusinessActivelyTradingController.show(index)) //TODO Route to next page
          }
        )
  }
}