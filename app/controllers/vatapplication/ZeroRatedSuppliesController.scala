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

package controllers.vatapplication

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.ZeroRatedSuppliesForm
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.ZeroRated
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.ZeroRatedSupplies

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZeroRatedSuppliesController @Inject()(val sessionService: SessionService,
                                            val authConnector: AuthClientConnector,
                                            vatApplicationService: VatApplicationService,
                                            zeroRatesSuppliesView: ZeroRatedSupplies
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val missingDataSection = "tasklist.vatRegistration.goodsAndServices"

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication =>
          (vatApplication.zeroRatedSupplies, vatApplication.turnoverEstimate) match {
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
            case (_, None) => throw MissingAnswerException(missingDataSection)
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getTurnover.flatMap {
          case Some(estimates) => ZeroRatedSuppliesForm.form(estimates).bindFromRequest.fold(
            errors => Future.successful(
              BadRequest(zeroRatesSuppliesView(
                routes.ZeroRatedSuppliesController.submit,
                errors
              ))),
            success => vatApplicationService.saveVatApplication(ZeroRated(success)) map { _ =>
              Redirect(routes.SellOrMoveNipController.show)
            }
          )
          case None => throw MissingAnswerException(missingDataSection)
        }
  }

}
