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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.StoringGoodsForm
import models.api.vatapplication.{StoringOverseas, StoringWithinUk}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.StoringGoods

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoringGoodsController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val vatApplicationService: VatApplicationService,
                                       formProvider: StoringGoodsForm,
                                       view: StoringGoods)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          vatApplication <- vatApplicationService.getVatApplication
          storingForDispatch = vatApplication.overseasCompliance.flatMap(_.storingGoodsForDispatch)
          filledForm = storingForDispatch.fold(formProvider.form)(formProvider.form.fill)
        } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        formProvider.form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors))),
          answer =>
            vatApplicationService.saveVatApplication(answer).map { _ =>
              answer match {
                case StoringWithinUk =>
                  Redirect(controllers.vatapplication.routes.DispatchFromWarehouseController.show)
                case StoringOverseas =>
                  Redirect(controllers.routes.TaskListController.show.url)
              }
            }
        )
  }

}
