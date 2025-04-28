/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.{StandardRateSuppliesForm, ZeroRatedSuppliesNewJourneyForm}
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.{StandardRate, Turnover, ZeroRated}
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.StandardRateSupplies

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StandardRateSuppliesController @Inject()(val sessionService: SessionService,
                                               val authConnector: AuthClientConnector,
                                               vatApplicationService: VatApplicationService,
                                               standardRatesSuppliesView: StandardRateSupplies
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val missingDataSection = "tasklist.vatRegistration.goodsAndServices"

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          val form = vatApplication.standardRateSupplies.fold(StandardRateSuppliesForm.form)(StandardRateSuppliesForm.form.fill)
          Ok(standardRatesSuppliesView(routes.StandardRateSuppliesController.submit,form))
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.flatMap { vatApplication => {
          (vatApplication.zeroRatedSupplies, vatApplication.reducedRateSupplies) match {
            case (Some(zeroRated), Some(reducedRated)) => StandardRateSuppliesForm.form.bindFromRequest.fold(
                errors => Future.successful(BadRequest(standardRatesSuppliesView(routes.StandardRateSuppliesController.submit,errors))),
                success => for {
                  _ <- vatApplicationService.saveVatApplication(StandardRate(success))
                  _ <- vatApplicationService.saveVatApplication(Turnover(zeroRated + reducedRated + success))
                } yield {
                  Redirect(routes.ReducedRateSuppliesController.show)
                }
              )
            case _ => StandardRateSuppliesForm.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(standardRatesSuppliesView(routes.StandardRateSuppliesController.submit,errors))),
              success => vatApplicationService.saveVatApplication(StandardRate(success)) map { _ =>
                Redirect(routes.ReducedRateSuppliesController.show)
              }
            )
          }
        }}
  }
}
