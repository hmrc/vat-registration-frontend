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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ImportsOrExportsForm
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.TradeVatGoodsOutsideUk
import services.{SessionService, VatApplicationService}
import views.html.vatapplication.ImportsOrExports

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportsOrExportsController @Inject()(val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           val vatApplicationService: VatApplicationService,
                                           view: ImportsOrExports)
                                          (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map {
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
          success => vatApplicationService.saveVatApplication(TradeVatGoodsOutsideUk(success)).map { _ =>
            if (success) {
              Redirect(controllers.vatapplication.routes.ApplyForEoriController.show)
            } else {
              Redirect(controllers.vatapplication.routes.TurnoverEstimateController.show)
            }
          }
        )
  }

}
