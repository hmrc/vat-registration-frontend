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

package controllers.returns

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ZeroRatedSuppliesForm
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException
import views.html.returns.zero_rated_supplies

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZeroRatedSuppliesController @Inject()(val sessionService: SessionService,
                                            val authConnector: AuthClientConnector,
                                            returnsService: ReturnsService,
                                            vatRegistrationService: VatRegistrationService,
                                            zeroRatesSuppliesView: zero_rated_supplies
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns flatMap { returns =>
          vatRegistrationService.fetchTurnoverEstimates map { optEstimates =>
            (returns.zeroRatedSupplies, optEstimates) match {
              case (Some(zeroRatedSupplies), Some(estimates)) =>
                Ok(zeroRatesSuppliesView(
                  routes.ZeroRatedSuppliesController.submit,
                  ZeroRatedSuppliesForm.form(estimates).fill(zeroRatedSupplies)
                ))
              case (None, Some(estimates)) =>
                Ok(zeroRatesSuppliesView(
                  routes.ZeroRatedSuppliesController.submit,
                  ZeroRatedSuppliesForm.form(estimates)
                ))
              case (_, None) => throw new InternalServerException("[ZeroRatedSuppliesController][show] Did not find user's turnover estimates")
            }
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        vatRegistrationService.fetchTurnoverEstimates flatMap {
          case Some(estimates) => ZeroRatedSuppliesForm.form(estimates).bindFromRequest.fold(
            errors => Future.successful(
              BadRequest(zeroRatesSuppliesView(
                routes.ZeroRatedSuppliesController.submit,
                errors
              ))),
            success => returnsService.saveZeroRatesSupplies(success) map { _ =>
              Redirect(routes.SellOrMoveNipController.show)
            }
          )
          case None => throw new InternalServerException("[ZeroRatedSuppliesController][submit] Did not find user's turnover estimates")
        }
  }

}
