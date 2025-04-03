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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ReducedRateSuppliesForm
import play.api.mvc._
import services.VatApplicationService.ReducedRate
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.ReducedRateSupplies

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReducedRateSuppliesController @Inject()(
                                             val sessionService: SessionService,
                                             val authConnector: AuthClientConnector,
                                             vatApplicationService: VatApplicationService,
                                             reducedRatedSuppliesView: ReducedRateSupplies,
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          optReducedRatedEstimate <- vatApplicationService.getReducedRated
          form = optReducedRatedEstimate.fold(ReducedRateSuppliesForm.form)(ReducedRateSuppliesForm.form.fill)
          page = Ok(reducedRatedSuppliesView(form))
        } yield page
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ReducedRateSuppliesForm.form.bindFromRequest.fold(
          errors => Future.successful(
            BadRequest(reducedRatedSuppliesView(errors))
          ),
          success => vatApplicationService.saveVatApplication(ReducedRate(success)) map { _ =>
            Redirect(routes.ZeroRatedSuppliesController.show)
          }
        )
  }

}