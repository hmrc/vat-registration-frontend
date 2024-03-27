/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.PaymentFrequencyForm
import models.api.vatapplication.AASDetails
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.PaymentFrequencyView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentFrequencyController @Inject()(view: PaymentFrequencyView,
                                           val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           vatApplicationService: VatApplicationService
                                          )(implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          vatApplication.annualAccountingDetails match {
            case Some(AASDetails(Some(paymentFrequency), _)) => Ok(view(PaymentFrequencyForm.apply().fill(paymentFrequency)))
            case _ => Ok(view(PaymentFrequencyForm.apply()))
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        PaymentFrequencyForm.apply().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          paymentFrequency => vatApplicationService.saveVatApplication(paymentFrequency).map { _ =>
            Redirect(routes.PaymentMethodController.show)
          }
        )
  }
}
