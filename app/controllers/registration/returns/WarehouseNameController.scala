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
import controllers.BaseController
import featureswitch.core.config.NorthernIrelandProtocol
import forms.WarehouseNameForm
import models.api.returns.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService}
import views.html.returns.WarehouseNameView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WarehouseNameController @Inject()(val sessionService: SessionService,
                                        val authConnector: AuthClientConnector,
                                        val returnsService: ReturnsService,
                                        val warehouseNameView: WarehouseNameView)
                                       (implicit appConfig: FrontendAppConfig,
                                        val executionContext: ExecutionContext,
                                        baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.overseasCompliance match {
            case Some(OverseasCompliance(_, _, _, _, _, Some(warehouseName))) =>
              Ok(warehouseNameView(WarehouseNameForm.form.fill(warehouseName)))
            case _ =>
              Ok(warehouseNameView(WarehouseNameForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        WarehouseNameForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(warehouseNameView(errors))),
          success => {
            for {
              returns <- returnsService.getReturns
              updatedReturns = returns.copy(
                overseasCompliance = returns.overseasCompliance.map(_.copy(
                  fulfilmentWarehouseName = Some(success)
                ))
              )
              _ <- returnsService.submitReturns(updatedReturns)
            } yield {
              if(isEnabled(NorthernIrelandProtocol))
                Redirect(routes.SellOrMoveNipController.show)
              else
                Redirect(routes.ReturnsController.returnsFrequencyPage)
            }
          }
        )
  }
}
