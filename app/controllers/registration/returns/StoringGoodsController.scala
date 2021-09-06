/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.StoringGoodsForm
import models.api.returns.{StoringOverseas, StoringWithinUk}
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile}
import views.html.StoringGoods

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoringGoodsController @Inject()(val keystoreConnector: KeystoreConnector,
                                       val authConnector: AuthClientConnector,
                                       val returnsService: ReturnsService,
                                       formProvider: StoringGoodsForm,
                                       view: StoringGoods)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  // TODO: Redirect to here if NO selected on "Will business send goods directly to overseas customers" page
  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      for {
        returns <- returnsService.getReturns
        storingForDispatch = returns.overseasCompliance.flatMap(_.storingGoodsForDispatch)
        filledForm = storingForDispatch.fold(formProvider.form)(formProvider.form.fill)
      } yield Ok(view(filledForm))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      formProvider.form.bindFromRequest.fold (
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        answer =>
          for {
            returns <- returnsService.getReturns
            updatedCompliance = returns.overseasCompliance.map {
              case compliance if answer.eq(StoringOverseas) =>
                compliance.copy(
                  storingGoodsForDispatch = Some(answer),
                  usingWarehouse = None,
                  fulfilmentWarehouseNumber = None,
                  fulfilmentWarehouseName = None
                )
              case compliance if answer.eq(StoringWithinUk) =>
                compliance.copy(
                  storingGoodsForDispatch = Some(answer)
                )
          }
            updatedReturns = returns.copy(overseasCompliance = updatedCompliance)
            _ <- returnsService.submitReturns(updatedReturns)
          } yield answer match {
            case StoringWithinUk =>
              Redirect(controllers.registration.returns.routes.DispatchFromWarehouseController.show)
            case StoringOverseas =>
              Redirect(controllers.registration.returns.routes.ReturnsController.returnsFrequencyPage)
          }
      )
  }

}
