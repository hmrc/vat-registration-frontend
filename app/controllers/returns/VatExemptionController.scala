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
import forms.{ChargeExpectancyForm, VatExemptionForm}
import models.{NonUk, TransferOfAGoingConcern}
import models.api.{NETP, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.{ReturnsService, SessionProfile, SessionService, VatRegistrationService}
import views.html.returns.{VatExemption, claim_refunds_view}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatExemptionController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val returnsService: ReturnsService,
                                       val vatRegistrationService: VatRegistrationService,
                                       val vatExemptionView: VatExemption)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        returnsService.getReturns map { returns =>
          returns.appliedForExemption match {
            case Some(appliedForExemption) => Ok(vatExemptionView(VatExemptionForm.form.fill(appliedForExemption)))
            case None => Ok(vatExemptionView(VatExemptionForm.form))
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        VatExemptionForm.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(vatExemptionView(errors))),
          success => {
            for {
              _ <- returnsService.saveVatExemption(success)
              registrationReason <- vatRegistrationService.getEligibilitySubmissionData.map(_.registrationReason)
            } yield registrationReason match {
              case TransferOfAGoingConcern =>
                Redirect(controllers.returns.routes.ReturnsController.returnsFrequencyPage)
              case NonUk =>
                Redirect(routes.SendGoodsOverseasController.show)
              case _ =>
                Redirect(routes.VatRegStartDateResolverController.resolve)
            }
          }
        )
  }
}
