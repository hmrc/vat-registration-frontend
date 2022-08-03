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
import featureswitch.core.config.{TaskList, TaxRepPage}
import forms.PaymentMethodForm
import models.api.vatapplication.AASDetails
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.aas_payment_method

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentMethodController @Inject()(val authConnector: AuthClientConnector,
                                        val sessionService: SessionService,
                                        view: aas_payment_method,
                                        vatApplicationService: VatApplicationService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          vatApplication.annualAccountingDetails match {
            case Some(AASDetails(_, Some(paymentMethod))) => Ok(view(PaymentMethodForm.apply().fill(paymentMethod)))
            case _ => Ok(view(PaymentMethodForm.apply()))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        PaymentMethodForm.apply().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          paymentMethod => vatApplicationService.saveVatApplication(paymentMethod).map { _ =>
            if (isEnabled(TaxRepPage)) {
              Redirect(controllers.vatapplication.routes.TaxRepController.show)
            } else {
              if (isEnabled(TaskList)) {
                Redirect(controllers.routes.TaskListController.show)
              } else {
                Redirect(controllers.flatratescheme.routes.JoinFlatRateSchemeController.show)
              }
            }
          }
        )
  }
}
