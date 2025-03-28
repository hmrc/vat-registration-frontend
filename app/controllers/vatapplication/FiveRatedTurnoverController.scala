/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.vatapplication

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.FiveRatedTurnoverForm
import services.VatApplicationService.FiveRated
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.FiveRatedTurnover

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FiveRatedTurnoverController @Inject()(
                                             val sessionService: SessionService,
                                             val authConnector: AuthClientConnector,
                                             vatApplicationService: VatApplicationService,
                                             fiveRatedTurnoverView: FiveRatedTurnover
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          optFiveRatedEstimate <- vatApplicationService.getFiveRated
          form = optFiveRatedEstimate.fold(FiveRatedTurnoverForm.form)(FiveRatedTurnoverForm.form.fill)
          page = Ok(fiveRatedTurnoverView(form))
        } yield page
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        FiveRatedTurnoverForm.form.bindFromRequest.fold(
          errors => Future.successful(
            BadRequest(fiveRatedTurnoverView(errors))
          ),
          success => vatApplicationService.saveVatApplication(FiveRated(success)) map { _ =>
            Redirect(routes.TurnoverEstimateController.show)
          }
        )
  }

}