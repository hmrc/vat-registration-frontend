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
import forms.TotalTaxTurnoverEstimateForm
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.AcceptTurnOverEstimate
import services.{SessionProfile, SessionService, VatApplicationService}
import viewmodels.Formatters
import views.html.vatapplication.TotalTaxTurnoverEstimate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TotalTaxTurnoverEstimateController @Inject()(val sessionService: SessionService,
                                                   val authConnector: AuthClientConnector,
                                                   vatApplicationService: VatApplicationService,
                                                   formProvider: TotalTaxTurnoverEstimateForm,
                                                   view: TotalTaxTurnoverEstimate
                                          )(implicit val executionContext: ExecutionContext,
                                            appConfig: FrontendAppConfig,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map {
          vatApp => {
            Ok(view(formProvider().fill(vatApp.acceptTurnOverEstimate.fold(false)(fg=>fg)), getAmount(vatApp.standardRateSupplies),
              getAmount(vatApp.reducedRateSupplies), getAmount(vatApp.zeroRatedSupplies), getAmount(vatApp.turnoverEstimate)))
          }
        }
  }


  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.flatMap { vatApp =>
          formProvider().bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, getAmount(vatApp.standardRateSupplies),
                getAmount(vatApp.reducedRateSupplies), getAmount(vatApp.zeroRatedSupplies), getAmount(vatApp.turnoverEstimate)))),

            success => vatApplicationService.saveVatApplication(AcceptTurnOverEstimate(success)).map { _ =>
              vatApplicationService.raiseAuditEvent(vatApp)
              if (success) {
                Redirect(controllers.vatapplication.routes.SellOrMoveNipController.show)
              } else {
                Redirect(controllers.vatapplication.routes.StandardRateSuppliesController.show)
              }
            }
          )
        }
  }

  private def getAmount(amt: Option[BigDecimal]): Option[String] = {
    amt.map(Formatters.currency)
  }

}