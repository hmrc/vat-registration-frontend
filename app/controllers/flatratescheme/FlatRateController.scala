/*
 * Copyright 2023 HM Revenue & Customs
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
import forms._
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.FlatRateService.{OverBusinessGoodsAnswer, OverBusinessGoodsPercentAnswer, UseThisRateAnswer}
import services._
import views.html.flatratescheme._

import java.text.DecimalFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FlatRateController @Inject()(val flatRateService: FlatRateService,
                                   val authConnector: AuthClientConnector,
                                   val sessionService: SessionService,
                                   annual_costs_inclusive: annual_costs_inclusive,
                                   annual_costs_limited: annual_costs_limited,
                                   frs_register_for: frs_register_for,
                                   frs_your_flat_rate: frs_your_flat_rate)
                                  (implicit appConfig: FrontendAppConfig,
                                   val executionContext: ExecutionContext,
                                   baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val registerForFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form()("frs.registerFor")
  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("joinFrs")("frs.join")
  val yourFlatRateForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form()("frs.registerForWithSector")
  val overBusinessGoodsForm: Form[Boolean] = OverBusinessGoodsForm.form

  def overBusinessGoodsPercentForm(formPct: BigDecimal = 0): Form[Boolean] = new OverBusinessGoodsPercentForm {
    override val pct: BigDecimal = formPct
  }.form

  def annualCostsInclusivePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val viewForm = flatRateScheme.overBusinessGoods.fold(overBusinessGoodsForm)(overBusinessGoodsForm.fill)
          Ok(annual_costs_inclusive(viewForm))
        }
  }

  def submitAnnualInclusiveCosts: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        overBusinessGoodsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(annual_costs_inclusive(badForm))),
          answer => flatRateService.saveFlatRate(OverBusinessGoodsAnswer(answer)).map { _ =>
            if (answer) {
              Redirect(controllers.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales)
            } else {
              Redirect(controllers.flatratescheme.routes.FlatRateController.registerForFrsPage)
            }
          }
        )
  }

  def annualCostsLimitedPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          val viewForm = flatRateScheme.overBusinessGoodsPercent.fold(form)(form.fill)
          Ok(annual_costs_limited(viewForm, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get)))
        }
  }

  def submitAnnualCostsLimited: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = overBusinessGoodsPercentForm(flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))
          form.bindFromRequest().fold(formErr => {
            Future.successful(BadRequest(annual_costs_limited(formErr, flatRateService.applyPercentRoundUp(flatRateScheme.estimateTotalSales.get))))
          },
            answer => flatRateService.saveFlatRate(OverBusinessGoodsPercentAnswer(answer)).map { _ =>
              if (answer) {
                Redirect(controllers.flatratescheme.routes.ConfirmBusinessTypeController.show)
              } else {
                Redirect(controllers.flatratescheme.routes.FlatRateController.registerForFrsPage)
              }
            }
          )
        }
  }

  def registerForFrsPage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate map { flatRateScheme =>
          val form = flatRateScheme.useThisRate match {
            case Some(useRate) => registerForFrsForm.fill(YesOrNoAnswer(useRate))
            case None => registerForFrsForm
          }
          Ok(frs_register_for(form))
        }
  }

  def submitRegisterForFrs: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        registerForFrsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(frs_register_for(badForm))),
          view => flatRateService.saveRegister(view.answer) map { _ =>
            if (view.answer) {
              Redirect(controllers.flatratescheme.routes.StartDateController.show)
            } else {
              Redirect(controllers.routes.TaskListController.show)
            }
          }
        )
  }

  def yourFlatRatePage: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        flatRateService.getFlatRate flatMap { flatRateScheme =>
          val form = flatRateScheme.useThisRate match {
            case Some(useRate) => yourFlatRateForm.fill(YesOrNoAnswer(useRate))
            case None => yourFlatRateForm
          }
          flatRateService.retrieveBusinessTypeDetails.map { businessTypeDetails =>
            val decimalFormat = new DecimalFormat("#0.##")
            Ok(frs_your_flat_rate(businessTypeDetails.businessTypeLabel, decimalFormat.format(businessTypeDetails.percentage), form))
          }
        }
  }

  def submitYourFlatRate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        yourFlatRateForm.bindFromRequest().fold(
          badForm => flatRateService.retrieveBusinessTypeDetails.map { businessTypeDetails =>
            val decimalFormat = new DecimalFormat("#0.##")
            BadRequest(frs_your_flat_rate(businessTypeDetails.businessTypeLabel, decimalFormat.format(businessTypeDetails.percentage), badForm))
          },
          view => for {
            _ <- flatRateService.saveFlatRate(UseThisRateAnswer(view.answer))
          } yield {
            if (view.answer) {
              Redirect(controllers.flatratescheme.routes.StartDateController.show)
            } else {
              Redirect(controllers.routes.TaskListController.show)
            }
          }
        )
  }
}
