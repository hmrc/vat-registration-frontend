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

package controllers.flatratescheme

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.OverBusinessGoodsPercentForm
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.OverBusinessGoodsPercentAnswer
import services.{FlatRateService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.flatratescheme.AnnualCostsLimited

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AnnualCostsLimitedController @Inject()(val flatRateService: FlatRateService,
                                               val authConnector: AuthConnector,
                                               val sessionService: SessionService,
                                               view: AnnualCostsLimited)
                                              (implicit appConfig: FrontendAppConfig,
                                               val executionContext: ExecutionContext,
                                               baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {


  def overBusinessGoodsPercentForm(formPct: BigDecimal = 0): Form[Boolean] = new OverBusinessGoodsPercentForm {
    override val pct: BigDecimal = formPct
  }.form

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          val viewForm = flatRateScheme.overBusinessGoodsPercent.fold(form)(form.fill)
          Ok(view(viewForm, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get)))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          form.bindFromRequest().fold(formErr => {
            Future.successful(BadRequest(view(formErr, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))))
          },
            answer => flatRateService.saveFlatRate(OverBusinessGoodsPercentAnswer(answer)).map { _ =>
              if (answer) {
                Redirect(controllers.flatratescheme.routes.ConfirmBusinessTypeController.show)
              } else {
                Redirect(controllers.flatratescheme.routes.RegisterForFrsController.show)
              }
            }
          )
        }
  }

}
