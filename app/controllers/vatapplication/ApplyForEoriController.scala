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
import forms.ApplyForEoriForm
import models.api.{EligibilitySubmissionData, NETP, NonUkNonEstablished}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Request}
import services.VatApplicationService.EoriRequested
import services.{SessionProfile, SessionService, VatApplicationService, VatRegistrationService}
import views.html.vatapplication.{OverseasApplyForEori, ApplyForEori => ApplyForEoriView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import featuretoggle.FeatureSwitch.TaxableTurnoverJourney
import featuretoggle.FeatureToggleSupport.isEnabled

@Singleton
class ApplyForEoriController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       vatRegistrationService: VatRegistrationService,
                                       vatApplicationService: VatApplicationService,
                                       applyForEoriView: ApplyForEoriView,
                                       overseasApplyForEoriView: OverseasApplyForEori)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  val show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatRegistrationService.getEligibilitySubmissionData.flatMap { eligibilityData =>
          vatApplicationService.getVatApplication.map {
            _.eoriRequested match {
              case Some(eoriRequested) => Ok(getView(eligibilityData, ApplyForEoriForm.form.fill(eoriRequested)))
              case None => Ok(getView(eligibilityData, ApplyForEoriForm.form))
            }
          }
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        ApplyForEoriForm.form.bindFromRequest.fold(
          errors =>
            vatRegistrationService.getEligibilitySubmissionData.map { eligibilityData =>
              BadRequest(getView(eligibilityData, errors))
            },
          success =>
            vatApplicationService.saveVatApplication(EoriRequested(success)).map { _ =>
              val isTTJourneyEnabled = isEnabled(TaxableTurnoverJourney)
              if(isTTJourneyEnabled) {
                Redirect(controllers.vatapplication.routes.TwentyRatedSuppliesController.show)
              } else {
                Redirect(controllers.vatapplication.routes.TurnoverEstimateController.show)
              }
            }
        )
  }

  private def getView(eligibilityData: EligibilitySubmissionData, form: Form[Boolean])
                     (implicit request: Request[_]) = {
    eligibilityData.partyType match {
      case NETP | NonUkNonEstablished if !eligibilityData.fixedEstablishmentInManOrUk => overseasApplyForEoriView(form)
      case _ => applyForEoriView(form)
    }
  }
}