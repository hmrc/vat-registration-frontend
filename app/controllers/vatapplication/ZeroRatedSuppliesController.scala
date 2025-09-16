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
import featuretoggle.FeatureSwitch.TaxableTurnoverJourney
import featuretoggle.FeatureToggleSupport
import forms.{ZeroRatedSuppliesForm, ZeroRatedSuppliesNewJourneyForm}
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.VatApplicationService.{Turnover, ZeroRated}
import services.{SessionProfile, SessionService, VatApplicationService}
import views.html.vatapplication.{ZeroRatedSupplies, ZeroRatedSuppliesNewJourney}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZeroRatedSuppliesController @Inject()(val sessionService: SessionService,
                                            val authConnector: AuthClientConnector,
                                            vatApplicationService: VatApplicationService,
                                            zeroRatesSuppliesOldView: ZeroRatedSupplies,
                                            zeroRatesSuppliesNewView: ZeroRatedSuppliesNewJourney
                                           )(implicit val executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  val missingDataSection = "tasklist.vatRegistration.goodsAndServices"

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatApplicationService.getVatApplication.map { vatApplication => {
          val isNewJourney = isEnabled(TaxableTurnoverJourney)
          val view = if(isNewJourney) zeroRatesSuppliesNewView.apply _ else zeroRatesSuppliesOldView.apply _

          if(isNewJourney) {
            val form = ZeroRatedSuppliesNewJourneyForm.form()
            Ok(view(routes.ZeroRatedSuppliesController.submit, vatApplication.zeroRatedSupplies.fold(form)(form.fill)))
          } else {
            (vatApplication.zeroRatedSupplies, vatApplication.turnoverEstimate) match {
              case (Some(zeroRatedSupplies), Some(estimates)) =>
                Ok(view(routes.ZeroRatedSuppliesController.submit, ZeroRatedSuppliesForm.form(estimates).fill(zeroRatedSupplies)))
              case (None, Some(estimates)) =>
                Ok(view(routes.ZeroRatedSuppliesController.submit, ZeroRatedSuppliesForm.form(estimates)))
              case (_, None) => throw MissingAnswerException(missingDataSection)
            }
          }
        }}
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile => {
        val isNewJourney = isEnabled(TaxableTurnoverJourney)
        val view = if(isNewJourney) zeroRatesSuppliesNewView.apply _ else zeroRatesSuppliesOldView.apply _

        if(isNewJourney){
          vatApplicationService.getVatApplication.flatMap { vatApplication => {
            (vatApplication.standardRateSupplies, vatApplication.reducedRateSupplies) match {
              case (Some(standardRated), Some(reducedRated)) =>
                ZeroRatedSuppliesNewJourneyForm.form().bindFromRequest().fold(
                  errors => Future.successful(BadRequest(view(routes.ZeroRatedSuppliesController.submit, errors))),
                  success => for {
                    _ <- vatApplicationService.saveVatApplication(ZeroRated(success))
                    _ <- vatApplicationService.saveVatApplication(Turnover(standardRated + reducedRated + success))
                  } yield {
                    val isTTJourneyEnabled = isEnabled(TaxableTurnoverJourney)
                    if(isTTJourneyEnabled) {
                      Redirect(routes.TotalTaxTurnoverEstimateController.show)
                    } else {
                      Redirect(routes.SellOrMoveNipController.show)
                    }
                  }
                )
              case _ => throw MissingAnswerException(missingDataSection)
            }
          }}
        } else {
          vatApplicationService.getTurnover.flatMap {
            case Some(estimates) => ZeroRatedSuppliesForm.form(estimates).bindFromRequest().fold(
              errors => Future.successful(BadRequest(view(routes.ZeroRatedSuppliesController.submit, errors))),
              success => vatApplicationService.saveVatApplication(ZeroRated(success)) map { _ =>
                Redirect(routes.SellOrMoveNipController.show)
              }
            )
            case None => throw MissingAnswerException(missingDataSection)
          }
        }
      }
  }
}
