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
import forms.DispatchFromWarehouseForm
import models.api.returns.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile}
import views.html.returns.DispatchFromWarehouseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DispatchFromWarehouseController @Inject()(val keystoreConnector: KeystoreConnector,
                                                val authConnector: AuthClientConnector,
                                                val returnsService: ReturnsService,
                                                val dispachFromWarehouseView: DispatchFromWarehouseView)
                                               (implicit appConfig: FrontendAppConfig,
                                                val executionContext: ExecutionContext,
                                                baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.overseasCompliance match {
            case Some(OverseasCompliance(_, _, _, Some(usingWarehouse), _)) =>
              Ok(dispachFromWarehouseView(DispatchFromWarehouseForm.form.fill(usingWarehouse)))
            case _ =>
              Ok(dispachFromWarehouseView(DispatchFromWarehouseForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        DispatchFromWarehouseForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(dispachFromWarehouseView(errors))),
          success => {
            for {
              returns <- returnsService.getReturns
              updatedReturns = returns.copy(
                overseasCompliance = returns.overseasCompliance.map(_.copy(
                  usingWarehouse = Some(success)
                ))
              )
              _ <- returnsService.submitReturns(updatedReturns)
            } yield {
              if (success) {
                Redirect(routes.DispatchFromWarehouseController.show) //TODO Update to route to warehouse number page
              }
              else {
                Redirect(routes.ReturnsController.mandatoryStartPage)
              }
            }
          }
        )
  }
}
