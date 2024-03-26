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

package controllers.flatratescheme

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.EstimateTotalSalesForm
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.EstimateTotalSalesAnswer
import services._
import views.html.flatratescheme.EstimateTotalSales

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EstimateTotalSalesController @Inject()(flatRateService: FlatRateService,
                                             val authConnector: AuthClientConnector,
                                             val sessionService: SessionService,
                                             estimateTotalSalesPage: EstimateTotalSales
                                            )(implicit appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext,
                                              baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = flatRateScheme.estimateTotalSales.fold(EstimateTotalSalesForm.form)(v => EstimateTotalSalesForm.form.fill(v))
          Ok(estimateTotalSalesPage(form))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        EstimateTotalSalesForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(estimateTotalSalesPage(badForm))),
          data => flatRateService.saveFlatRate(EstimateTotalSalesAnswer(data)) map {
            _ => Redirect(controllers.flatratescheme.routes.AnnualCostsLimitedController.show)
          }
        )
  }
}
