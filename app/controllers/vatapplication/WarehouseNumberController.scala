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
import forms.WarehouseNumberForm
import models.api.vatapplication.OverseasCompliance
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.WarehouseNumber
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.WarehouseNumberView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WarehouseNumberController @Inject()(val sessionService: SessionService,
                                          val authConnector: AuthClientConnector,
                                          val vatApplicationService: VatApplicationService,
                                          val warehouseNumberView: WarehouseNumberView)
                                         (implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication map { vatApplication =>
          vatApplication.overseasCompliance match {
            case Some(OverseasCompliance(_, _, _, _, Some(warehouseNumber), _)) =>
              Ok(warehouseNumberView(WarehouseNumberForm.form.fill(warehouseNumber)))
            case _ =>
              Ok(warehouseNumberView(WarehouseNumberForm.form))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        WarehouseNumberForm.form.bindFromRequest().fold(
          errors => Future.successful(BadRequest(warehouseNumberView(errors))),
          success => {
            vatApplicationService.saveVatApplication(WarehouseNumber(success)).map { _ =>
              Redirect(routes.WarehouseNameController.show)
            }
          }
        )
  }
}
