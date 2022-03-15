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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ImportsOrExportsForm
import forms.ImportsOrExportsForm.{form => importsOrExportsForm}
import play.api.mvc.{Action, AnyContent}
import services.{SessionService, TradingDetailsService}
import views.html.ImportsOrExports

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportsOrExportsController @Inject()(val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           val tradingDetailsService: TradingDetailsService,
                                           view: ImportsOrExports)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        tradingDetailsService.getTradingDetailsViewModel(profile.registrationId) map {
          _.tradeVatGoodsOutsideUk match {
            case Some(importsOrExports) => Ok(view(ImportsOrExportsForm.form.fill(importsOrExports)))
            case _ => Ok(view(ImportsOrExportsForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ImportsOrExportsForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(view(errors))),
          success => tradingDetailsService.saveTradeVatGoodsOutsideUk(profile.registrationId, success) map { _ =>
            if (success) {
              Redirect(controllers.registration.business.routes.ApplyForEoriController.show)
            } else {
              Redirect(controllers.registration.returns.routes.ZeroRatedSuppliesResolverController.resolve)
            }
          }
        )
  }

}
