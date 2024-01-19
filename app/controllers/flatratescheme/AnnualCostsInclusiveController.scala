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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.OverBusinessGoodsForm
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.OverBusinessGoodsAnswer
import services.{FlatRateService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.flatratescheme.AnnualCostsInclusive

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnnualCostsInclusiveController @Inject()(val flatRateService: FlatRateService,
                                               val authConnector: AuthConnector,
                                               val sessionService: SessionService,
                                               view: AnnualCostsInclusive)
                                              (implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val overBusinessGoodsForm: Form[Boolean] = OverBusinessGoodsForm.form

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val viewForm = flatRateScheme.overBusinessGoods.fold(overBusinessGoodsForm)(overBusinessGoodsForm.fill)
          Ok(view(viewForm))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        overBusinessGoodsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          answer => flatRateService.saveFlatRate(OverBusinessGoodsAnswer(answer)).map { _ =>
            if (answer) {
              Redirect(controllers.flatratescheme.routes.EstimateTotalSalesController.show)
            } else {
              Redirect(controllers.flatratescheme.routes.RegisterForFrsController.show)
            }
          }
        )
  }

}
