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

package controllers.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.TurnoverEstimateForm
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService, VatRegistrationService}
import views.html.returns.TurnoverEstimate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TurnoverEstimateController @Inject()(val sessionService: SessionService,
                                           val authConnector: AuthClientConnector,
                                           returnsService: ReturnsService,
                                           turnoverEstimateView: TurnoverEstimate
                                          )(implicit val executionContext: ExecutionContext,
                                            appConfig: FrontendAppConfig,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          optTurnoverEstimate <- returnsService.getTurnover
          form = optTurnoverEstimate.fold(TurnoverEstimateForm.form)(TurnoverEstimateForm.form.fill)
          page = Ok(turnoverEstimateView(form))
        } yield page
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        TurnoverEstimateForm.form.bindFromRequest.fold(
          errors => Future.successful(
            BadRequest(turnoverEstimateView(errors))
          ),
          success => returnsService.saveTurnover(success) map { _ =>
            Redirect(routes.ZeroRatedSuppliesResolverController.resolve)
          }
        )
  }

}
